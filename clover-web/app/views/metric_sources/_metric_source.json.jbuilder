json.extract! metric_source, :id, :host, :port, :database, :created_at, :updated_at
json.url metric_source_url(metric_source, format: :json)
