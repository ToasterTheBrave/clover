require 'date'
require 'net/http'

module TrafficSimulator

  def TrafficSimulator.run(url, requestsPerSecond)

    trap('SIGINT') do
      exit 0
    end

    1.step do |i|
      requestsPerSecond.times do |requestNum|
        Thread.new do
          startTime = DateTime.now
          response = Net::HTTP.get_response('localhost', url)
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
          puts output
          STDOUT.flush
        end
      end
      sleep 1
    end

  end

end

TrafficSimulator::run(ARGV[0], ARGV[1].to_i)
