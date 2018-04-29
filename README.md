# Tyler Ruppert's Master Project - Clover

Clover is a root cause analysis system.  There are three separate systems contained in this same repo.  These would normally belong in separate repos, but are contained here for simplicity.

## Example System

Clover analyzes a lot of data, And it relies on the unpredictabily provided only by a running system.  For this reason, I've built a simple, but practical example system to run and produce data.

This example system consists of a load balancer, 2 web servers, a MySQL server, and an Elasticsearch cluster consisting of 5 nodes (3 master, 2 data)

Metric extractors are set up on this system using Telegraf, sending the data to InfluxDB.  You can also set up Grafana to view these metrics.

## Clover

Now that we have an example system providing us with data, we can use Clover to transform, evaluate, alert, and give insights via a report on this data.

#### Setup

Clover is meant to run via Apache Spark.  You can find the instructions for installing Spark [in the Spark docs](https://spark.apache.org/docs/latest)

After cloning this repo, run `sbt clean assembly`
This will create a jar file in the target directory.  This jar will be used for actually running Clover.

#### Config

Clover requires configuration in order to know which metrics to run.  One key aspect of Clover is that you don't have to understand the metrics, but you do have to tell Clover where to find them.  There are examples in clover/src/main/resources/.

#### Transforming Data

In order to run the transformer, run:
```
$SPARK_HOME/bin/spark-submit \
  --class "clover.service.TransformMetrics" \
  path/to/clover/jarfile.jar all
```

where `all` is the name of the configuration file

#### Training Models

```
$SPARK_HOME/bin/spark-submit \
  --class "clover.service.TrainAlgorithm" \
  path/to/clover/jarfile.jar all
```

where `all` is the name of the configuration file

#### Evaluating Metrics

```
$SPARK_HOME/bin/spark-submit \
  --class "clover.service.EvaluateMetrics" \
  path/to/clover/jarfile.jar all
```

where `all` is the name of the configuration file


## Clover Web

Clover Web is a Ruby on rails application for managing alerts and reports.  
#### Setup

To run Clover Web, cd into the directory clover-web and run:

```
bundle install
rake db:migrate
rails server
```

Before reporting will work, we need to download the most recent jar file from https://s3.amazonaws.com/master-project-clover/clover.jar and place it in clover-web/bin/clover.jar

#### Alerting

Alerting is run via rake task.  In the terminal, cd to clover-web and run:

```
rake run_alerting_service
```