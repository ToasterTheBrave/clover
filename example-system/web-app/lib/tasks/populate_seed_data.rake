
files = [
  # "/app/web-app/data/ny-archive/2017-01.json",
  # "/app/web-app/data/ny-archive/2017-02.json",
  # "/app/web-app/data/ny-archive/2017-03.json",
  # "/app/web-app/data/ny-archive/2017-04.json",
  # "/app/web-app/data/ny-archive/2017-05.json",
  # "/app/web-app/data/ny-archive/2017-06.json",
  # "/app/web-app/data/ny-archive/2017-07.json",
  # "/app/web-app/data/ny-archive/2017-08.json",
  # "/app/web-app/data/ny-archive/2017-09.json",
  # "/app/web-app/data/ny-archive/2017-10.json",
  "/app/web-app/data/ny-archive/2017-11.json"
  # "/app/web-app/data/ny-archive/2017-12.json"
]

trap('SIGINT') do
  puts "Exiting"
  exit 0
end

task :populate_seed_data => :environment do
  # Populate the MySQL database with data from the ny-archive
  files.each do |file|
    NYArchiveImporter.import(file)
    puts "Sleeping 10 seconds to allow interrupt time"
    sleep 10
  end
end
