class Article < ApplicationRecord
  include Elasticsearch::Model
  include Elasticsearch::Model::Callbacks

  settings index: {
    number_of_shards: 5,
    number_of_replicas: 1
  } do
    mapping dynamic: false do
      indexes :headline_main, analyzer: 'english'
      indexes :headline_content_kicker, analyzer: 'english'
      indexes :headline_kicker, analyzer: 'english'
      indexes :print_headline, analyzer: 'english'
      indexes :byline, analyzer: 'english'
      indexes :web_url
      indexes :snippet, analyzer: 'english'
      indexes :lead_paragraph, analyzer: 'english'
      indexes :print_page, type: 'integer'
      indexes :source, analyzer: 'english'
      indexes :pub_date, type: 'date'
      indexes :document_type, analyzer: 'english'
      indexes :section, analyzer: 'english'
      indexes :type_of_material, analyzer: 'english'
      indexes :word_count, type: 'integer'
    end
  end

  index_name Rails.application.class.parent_name.underscore
  document_type self.name.downcase

  Elasticsearch::Model.client = Elasticsearch::Client.new({
    host: '52.54.112.235'
  })

  def self.search(query)
    __elasticsearch__.search({
      query: {
        multi_match: {
          query: query,
          fields: [
            'headline_main^5',
            'headline_content_kicker',
            'headline_kicker',
            'print_headline^5',
            'byline',
            'snippet',
            'lead_paragraph',
            'section',
            'type_of_material'
          ]
        }
      }
    })
  end
end
