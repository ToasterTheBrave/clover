class CreateArticles < ActiveRecord::Migration[5.1]
  def change
    create_table :articles do |t|
      t.string :headline_main
      t.string :headline_content_kicker
      t.string :headline_kicker
      t.string :print_headline
      t.string :byline
      t.string :web_url
      t.text :snippet
      t.text :lead_paragraph
      t.integer :print_page
      t.string :source
      t.datetime :pub_date
      t.string :document_type
      t.string :section
      t.string :type_of_material
      t.integer :word_count
    end
  end
end
