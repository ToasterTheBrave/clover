require 'json'

module NYArchiveImporter

  def NYArchiveImporter.import(filename)
    data = JSON.parse(File.read(filename))

    total = data["response"]["docs"].length
    current = 0

    data["response"]["docs"].each do |doc|
      current = current + 1
      puts "[#{filename}] #{current} of #{total}" if current % 10 == 0

      begin
        article = Article.new({
          :headline_main => doc["headline"]["main"],
          :headline_content_kicker => doc["headline"]["content_kicker"],
          :headline_kicker => doc["headline"]["print_headline"],
          :print_headline => doc["headline"]["print_headline"],
          :byline => (doc["byline"].class == "Hash" ? doc["byline"]["original"] : ""),
          :web_url => doc["web_url"],
          :snippet => doc["snippet"],
          :lead_paragraph => doc["lead_paragraph"],
          :print_page => doc["print_page"].to_i,
          :source => doc["source"],
          :pub_date => doc["pub_date"],
          :document_type => doc["document_type"],
          :section => doc["section_name"],
          :type_of_material => doc["type_of_material"],
          :word_count => doc["word_count"].to_i
          })

        article.save

        # TODO : make sure this indexes to ES correctly too

      rescue => e
        puts "Exception: " + e.message
        puts "Continuing on"
      end

    end

  end

end