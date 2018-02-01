json.extract! article, :id, :headline_main, :headline_content_kicker, :headline_kicker, :print_headline, :byline, :web_url, :snippet, :lead_paragraph, :print_page, :source, :pub_date, :document_type, :section, :type_of_material, :word_count, :created_at, :updated_at
json.url article_url(article, format: :json)
