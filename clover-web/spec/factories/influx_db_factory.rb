FactoryBot.define do
  factory :influxdb, class: InfluxDB::Client do
    database "traffic_simulator"
    username "root"
    password "root"
    host "localhost"
    port 8086
  end
end
