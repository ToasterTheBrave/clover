class CreateAlerts < ActiveRecord::Migration[5.1]
  def change
    create_table :alerts do |t|
      t.references :report, foreign_key: true
      t.string :status
      t.timestamp :triggered
      t.timestamp :resolved
    end
  end
end
