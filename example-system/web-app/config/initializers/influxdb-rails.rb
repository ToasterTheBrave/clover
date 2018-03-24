InfluxDB::Rails.configure do |config|
  config.influxdb_database = "rails_app"
  config.influxdb_username = "root"
  config.influxdb_password = "root"
  config.influxdb_hosts    = ["18.233.132.222"]
  config.influxdb_port     = 8086
end
