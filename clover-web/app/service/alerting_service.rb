require 'influxdb'

module AlertingService

  def self.run
    thresholds = Threshold.all

    threads = []

    thresholds.each do |threshold|
      threads << Thread.new do
        loopThreshold(threshold)
      end
    end

    threads.each { |thread|
      thread.join
    }

  end

  def self.loopThreshold(threshold)
    metricSource = threshold.metric.metric_source
    influxdb = InfluxDB::Client.new \
      database: metricSource.database,
      host: metricSource.host,
      port: metricSource.port,
      username: 'root',
      password: 'root'
    recent = []
    1.step do |i|
      recentData = getMostRecent(threshold, influxdb)
      recent << recentData[:value]

      recent = trimRecentArray(recent, threshold.count)

      alertIfNecessary(threshold, recent, recentData[:time])

      sleep threshold.interval
    end
  end

  def self.getMostRecent(threshold, influxdb)
    metric = threshold.metric
    resp = influxdb.query "select #{metric.value_field} from #{metric.measurement_name} order by time desc limit 1"
    row = resp[0]["values"][0]
    {
      time: row["time"],
      value: row[metric.value_field]
    }
  end

  def self.trimRecentArray(recent, count)
    while(recent.length > count)
      recent.shift
    end
    recent
  end

  def self.alertIfNecessary(threshold, recent, time)
    if shouldAlert?(threshold, recent)
      puts "#{threshold.id}: Triggering Alert!!! : [#{threshold.low_value} - #{threshold.high_value}] : #{recent} (#{threshold.count})"
      alert = triggerAlert(threshold, time)
      sendAlertEmails(threshold, alert)
    else
      puts "#{threshold.id}: [#{threshold.low_value} - #{threshold.high_value}] : #{recent} (#{threshold.count})"
    end
  end

  def self.shouldAlert?(threshold, recent)
    (
      threshold.reports.all.all? { |report|
        report.alerts.all.all? { |alert|
          alert.status != "open"
        }
      } &&
      recent.count == threshold.count &&
      recent.all? { |value|
        (value < threshold.low_value || value > threshold.high_value)
      }
    )
  end

  def self.triggerAlert(threshold, time)
    report = Report.create(timestamp: time, threshold: threshold)
    Alert.create(report: report, status: 'open', triggered: time)
  end

  def self.sendAlertEmails(threshold, alert)
    AlertMailer.with(threshold: threshold, alert: alert).alert_email.deliver
  end

end
