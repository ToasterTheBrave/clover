class User < ApplicationRecord
  has_many :alert_rules
  has_many :thresholds, through: :alert_rules
end
