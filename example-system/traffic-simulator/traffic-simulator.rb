require 'date'
require 'net/http'
require 'influxdb'

module TrafficSimulator

  def TrafficSimulator.run(url, requestsPerSecond)

    trap('SIGINT') do
      exit 01
    end

    influxdb = InfluxDB::Client.new ENV["INFLUXDB_DB"], host: ENV["INFLUXDB_HOST"], username: ENV["INFLUXDB_USER"], password: ENV["INFLUXDB_PASS"]

    1.step do |i|
      requestsPerSecond.times do |requestNum|
        Thread.new do
          startTime = DateTime.now
          response = Net::HTTP.get_response(ENV["WEBAPP_HOST"], url)
          endTime = DateTime.now
          output = {
            :url => url,
            :durationInMillis => (endTime.strftime('%Q').to_i - startTime.strftime('%Q').to_i),
            :code => response.code,
            :message => response.message,
            :bodyLength => response.body.length,
            :server => response.body[/Host: ([a-f0-9]+)/,1]
          }
          values = {
            :bodyLength => response.body.length,
            :message => response.message,
            :code => response.code,
            :durationInMillis => (endTime.strftime('%Q').to_i - startTime.strftime('%Q').to_i)
          }
          tags = {
            :server => response.body[/Host: ([a-f0-9]+)/,1],
            :url => url
          }
          influxdb.write_point("requests", {values: values, tags: tags}) unless tags[:server].nil?
          puts output
          STDOUT.flush
        end
      end
      sleep 1
    end

  end

end

TrafficSimulator::run(ARGV[0], ARGV[1].to_i)
