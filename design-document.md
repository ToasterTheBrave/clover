Clover - Software Design Document
==========

*By Tyler Ruppert  
UCCS*

Introduction
----------
In distributed systems, tracing the root cause of errors can be especially difficult.  The metrics we monitor for performance often do not give much insight into what is actually wrong with the system.  In web systems, for instance, the page load time is a very closely watched metric.  When this grows or spikes, there is no easy way to determine the cause.  There are simply too many metrics for a human being to monitor.  Small, seemingly insignificant metrics could potentially be the cause of catastrophic failure in a system.

The purpose of this software design document is to outline the layout and implementation of Clover, a root cause analysis system designed for distributed systems.  Clover continuously monitors all available metrics and models when each metric is normal or anomalous.  In the event of an anomaly in our watched metric, Clover builds a report of other relevant anomalous metrics leading up to the watched metric anomaly and alerts the user.

#### Scope
This document describes a root cause analysis system, named Clover.  Clover consists of 3 main services.  The first service processes all available metrics to determine what is normal, and what is anomalous.  The second service alerts a user when a watched metric becomes anomalous.  The third service builds a timeline report of all other metrics that became anomalous leading up to the anomaly in our watched metric. 

In order to give an example of Clover working, there are two additional pieces that must be included.  First, an example system to produce metrics for consumption and evaluation must be constructed.  This example system must also have failures injected into it.  Second, these metrics must be pushed to Clover.

#### Overview
This document starts with a high level overview of the three services that make up Clover.  Afterward, we will do a deep dive into the architecture of each service and explore how they work together to form a complete system.

#### Reference Material
**Docker**: A container technology that allows us to easily run multiple services in isolation on a shared host.  https://www.docker.com  
**Gremlins**: A Fault injector used to simulate issues in the example system.  https://github.com/allingeek/gremlins  
**cAdvisor**: A service that monitors resource usage and performance of running docker containers.  https://hub.docker.com/r/google/cadvisor  
**InfluxDB**: A time series database.  https://github.com/influxdata/influxdb

#### Definitions and Acronyms
**Clover**: The name given to this software.  
**Watched Metric**: A metric that has been determined to be of importance to the end user.  When this metric becomes anomalous, Clover will build a report and alert.

System Overview
----------
Clover consists of 3 services, but also relies on the existence of two other pieces.

![System Overview](https://raw.githubusercontent.com/truppert/clover/master/system-overview.png)

1. Example System: Standard setup of a web system architecture, consisting of servers, load balancers, and databases.
2. Metric Extractors: Extract all available metrics from our example system, saving the data in a data store where Clover can access it.
3. Metric Processing Service: Read in metrics and determine if each is normal or anomalous.  In the event that a watched metric is anomalous, invoke the alerting and report building services.
4. Alerting Service: Alert the user that an anomaly was detected in the watched metric.
5. Report Building Service: For a given watched metric anomaly, build a timeline of relevant anomalous metrics leading up to the watched metric anomaly.

System Architecture
----------

#### Example System
In order to show the usefulness of Clover, we need a test system to monitor and inject failure into.  This system exists outside of Clover and simulates a real-world architecture like would be seen in a common web application.  It is purposefully complex in order to show the usefulness of Clover.

The example system consists of a Nginx load balancer to direct traffic between 4 application servers.  Each application server makes a call to a MySQL Database, as well as to an Elasticsearch Cluster.  The data collected from these two data stores is returned to the user, and the response time of the request is our watched metric.

![Example System](https://raw.githubusercontent.com/truppert/clover/master/example-system.png)

This system will be set up using Docker.  We artificially inject failure into the example system using a tool called Gremlins.  With this tool, we can inject failure at different points in the system.

#### Metric Extractors
Once we have a running example system, we need to export metrics from this system.  The metrics that are interesting are things like cpu load, free disk space, and all other system-level metrics that we can track.  

This extraction of metrics is out of scope for Clover.  This is expected to already exist in a system, or to be set up separately.  Luckily, there are already tools to do this.  We will be using cAdvisor.  cAdvisor is provided as a docker image that runs along side our example systems and will extract metrics from it and store them in InfluxDB

In addition to the metrics provided by cAdvisor, we will also be tracking the page load time of our app.  Often times, this is taken from application log files.  We will be directly saving this metric in InfluxDB.

#### Metric Processing Service Design
The first service of Clover evaluates all available metrics passed in by our metric extractors (cAdvisor and our direct extractor for page load time).  These metrics are processed in real time and the evaluated value is stored in an data store for later use.

The metric processing service is the largest contribution of Clover, and will require the most work.  The evaluation of a plethora of metrics and determination of what is normal and what is anomalous paves the way for understanding the cascading failures of our system.

As metrics are streamed to InfluxDB, they will be instantly pulled in and processed by Clover.  In the first step (1), Clover pulls information from MySQL to determine what model is applied to each metric.  In this same step, we are filtering down to only metrics that have known models.  Next (2), a map reduce job is enacted to parallelize applying our models to metrics.  Once these models are applied, the new information we attain is the expected value, the current value, and the timestamp of the metric.  (3) This data is written to InfluxDB for later use by the report building service.

![Metric Processing Service](https://raw.githubusercontent.com/truppert/clover/master/metrics-processing-service.png)

There are different models applied to different metrics to determine what is "normal" for each metric.  For example, a disk space metric is considered normal as long as it remains consistent and below a specified threshold.  CPU load is considered normal as long as it remains within a range, never spiking.  These models are manually assigned to the metrics.  Such metadata for metrics is stored in a MySQL database and manually altered when necessary.

The metrics are evaluated on a sliding window of time.  This allows us to watch anomalies in reference to current, recent behavior and set thresholds based on a percentage range outside of the recent norm.

This service will be written in Scala, using Apache Spark.  Apache Spark allows us to scale nicely for a large number of metrics and has built in support for evaluating data in a sliding window of time.  It also has built in Machine Learning libraries that may come in handy as we are building our models.

When a metric becomes anomalous, it is simply noted as such in our data store.  When a tracked metric becomes anomalous, it is picked up and acted upon by the alerting service, requiring no special treatment by the metric processing service.

#### Alerting Service Design

We need to alert the user when our tracked metric becomes anomalous. The alerting service is designed to be sufficient, but minimal.  We will keep metadata on each alert in MySQL and track there if an alert remains open, or has been manually closed.  This prevents multiple alerts for a single anomaly.  (1) Every 10 seconds, we read current expected and actual values for each metric.  (2) If a metric is anomalous and (3) does not already have an open alert, (4) the report building service is called to build the report and the (5) the user is alerted via email with a link to the report.

![Alerting Service](https://raw.githubusercontent.com/truppert/clover/master/alerting-service.png)

#### Report Building Service Design

The final service needed is the report building service.  This service is called when our watched metric, response time, becomes anomalous.  We will call the time that this watched metric became anomalous our "report time," meaning the time the report became necessary.

By the time this service is called, we have already collected and evaluated all the necessary metrics.  We have already determined what metrics have been anomalous, when they became such, and how long they remained so.  This allows us to build a report of the state of the system leading up to our report time.

The report building service takes the top 10 anomalous metrics in the hour leading up to the report time.  Anomalies closer to the report time are weighted as more important.

The report data is saved in MySQL and displayed to the user in a single page web interface.  This web UI is built using Ruby on Rails, an easy to use web framework.  Previous reports remain available in the event they are later needed.

The report in the web UI shows a graph with time as the x-axis, and an anomaly delta from -1 to 1 on the y-axis.  0 is normal, and -1 and 1 are the maximum values of anomalous metrics in the data set.
