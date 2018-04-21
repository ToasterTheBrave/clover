FactoryBot.define do
  factory :metric do
    measurement_name 'example_measurement'
    value_field 'example_value_field'
    metric_source
  end
end
