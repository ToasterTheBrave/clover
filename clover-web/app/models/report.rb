class Report < ApplicationRecord
  belongs_to :threshold
  has_many :alerts
end
