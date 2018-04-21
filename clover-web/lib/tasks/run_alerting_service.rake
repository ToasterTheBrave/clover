trap('SIGINT') do
  puts "Exiting"
  exit 0
end

task :run_alerting_service => :environment do
  AlertingService.run()
end