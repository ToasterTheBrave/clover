FactoryBot.define do
  factory :metric_source do
    host 'example_host'
    port 8086
    database 'example_database'
  end
end