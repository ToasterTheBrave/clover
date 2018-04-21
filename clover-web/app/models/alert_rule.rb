class AlertRule < ApplicationRecord
  belongs_to :threshold
  belongs_to :user
end
