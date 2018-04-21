FactoryBot.define do
  factory :threshold do
    low_value 100
    high_value 300
    count 5
    interval 1
    metric
  end
end
