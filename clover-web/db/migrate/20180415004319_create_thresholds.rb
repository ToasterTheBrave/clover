class CreateThresholds < ActiveRecord::Migration[5.1]
  def change
    create_table :thresholds do |t|
      t.references :metric, foreign_key: true
      t.decimal :low_value
      t.decimal :high_value
      t.integer :count
      t.integer :interval
    end
  end
end
