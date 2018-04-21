class AddThresholdToReports < ActiveRecord::Migration[5.1]
  def change
    add_reference :reports, :threshold, foreign_key: true
  end
end
