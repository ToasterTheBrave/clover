json.extract! alert, :id, :report_id, :status, :triggered, :resolved, :created_at, :updated_at
json.url alert_url(alert, format: :json)
