require 'date'
require 'net/http'
require 'influxdb'

module TrafficSimulator

  def TrafficSimulator.run(url, requestsPerSecond)

    trap('SIGINT') do
      exit 01
    end

    influxdb = InfluxDB::Client.new "traffic_simulator", host: "18.233.132.222", username: "root", password: "root"

    1.step do |i|
      requestsPerSecond.times do |requestNum|
        Thread.new do
          startTime = DateTime.now
          response = Net::HTTP.get_response('52.54.112.235', url)
          endTime = DateTime.now
          output = {
            :iteration => i,
            :requestNum => requestNum + 1,
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
            :message => response.message,
            :code => response.code,
            :url => url,
            :iteration => i,
            :requestNum => requestNum + 1
          }
          influxdb.write_point("requests", {values: values, tags: tags})
          puts output
          STDOUT.flush
        end
      end
      sleep 1
    end

  end

end

TrafficSimulator::run(ARGV[0], ARGV[1].to_i)
