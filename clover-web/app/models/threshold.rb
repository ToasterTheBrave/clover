class Threshold < ApplicationRecord
  belongs_to :metric
  has_many :reports
  has_many :alert_rules
  has_many :users, through: :alert_rules
end
