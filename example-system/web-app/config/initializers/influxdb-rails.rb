InfluxDB::Rails.configure do |config|
  config.influxdb_database = "rails_app"
  config.influxdb_username = "root"
  config.influxdb_password = "root"
  config.influxdb_hosts    = ["52.54.112.235"]
  config.influxdb_port     = 8086
end
