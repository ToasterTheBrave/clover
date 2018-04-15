json.extract! metric, :id, :metric_source_id, :measurement_name, :value_field, :created_at, :updated_at
json.url metric_url(metric, format: :json)
