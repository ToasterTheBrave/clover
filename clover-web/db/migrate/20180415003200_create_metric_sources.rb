class CreateMetricSources < ActiveRecord::Migration[5.1]
  def change
    create_table :metric_sources do |t|
      t.string :host
      t.integer :port
      t.string :database
    end
  end
end
