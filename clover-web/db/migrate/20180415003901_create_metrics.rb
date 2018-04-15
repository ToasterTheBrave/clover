class CreateMetrics < ActiveRecord::Migration[5.1]
  def change
    create_table :metrics do |t|
      t.references :metric_source, foreign_key: true
      t.string :measurement_name
      t.string :value_field
    end
  end
end
