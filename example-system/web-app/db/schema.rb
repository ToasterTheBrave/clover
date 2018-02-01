# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20180111025123) do

  create_table "articles", force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=latin1" do |t|
    t.string "headline_main"
    t.string "headline_content_kicker"
    t.string "headline_kicker"
    t.string "print_headline"
    t.string "byline"
    t.string "web_url"
    t.text "snippet"
    t.text "lead_paragraph"
    t.integer "print_page"
    t.string "source"
    t.datetime "pub_date"
    t.string "document_type"
    t.string "section"
    t.string "type_of_material"
    t.integer "word_count"
  end

end
