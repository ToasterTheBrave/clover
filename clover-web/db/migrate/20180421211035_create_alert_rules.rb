class CreateAlertRules < ActiveRecord::Migration[5.1]
  def change
    create_table :alert_rules do |t|
      t.references :threshold, foreign_key: true
      t.references :user, foreign_key: true
    end
  end
end
