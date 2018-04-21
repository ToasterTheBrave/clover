require 'rails_helper'
require_relative '../../app/service/alerting_service.rb'

describe AlertingService do

  describe 'run' do
    # Side effects only.  Nothing to test
  end

  describe 'loopThreshold' do
    # Side effects only.  Nothing to test
  end

  describe 'getMostRecent' do
    it 'returns the single most recent value from the db' do
      threshold = build(:threshold)
      influxdb_client = double('influxdb')
      allow(influxdb_client).to receive(:query).and_return([{'values' => [{'time' => '2018-04-10T18:57:44Z', 'example_value_field' => 346}]}])
      recentData = AlertingService.getMostRecent(threshold, influxdb_client)
      expect(recentData[:time]).to eq('2018-04-10T18:57:44Z')
      expect(recentData[:value]).to eq(346)
    end
  end

  describe 'trimRecentArray' do
    it 'returns the array unchanged when below or equal to count' do
      recent = [123, 456, 789]
      expect(AlertingService.trimRecentArray(recent, 5)).to eq(recent)
      expect(AlertingService.trimRecentArray(recent, 3)).to eq(recent)
    end
    it 'drops from the front of array when over count' do
      recent = [123, 456, 789]
      expect(AlertingService.trimRecentArray(recent, 2)).to eq([456, 789])
      expect(AlertingService.trimRecentArray(recent, 1)).to eq([789])
    end
  end

  describe 'alertIfNecessary' do
    # Side effects only.  Nothing to test
  end

  describe 'shouldAlert?' do
    it 'returns true when ALL values are outside of the threshold' do
      threshold = build(:threshold)
      expect(AlertingService.shouldAlert?(threshold, [88, 89, 90, 91, 92])).to be true
      expect(AlertingService.shouldAlert?(threshold, [301, 302, 303, 304, 305])).to be true
      expect(AlertingService.shouldAlert?(threshold, [88, 89, 303, 304, 305])).to be true
    end

    it 'returns false when ANY values is outside of the threshold' do
      threshold = build(:threshold)
      expect(AlertingService.shouldAlert?(threshold, [250, 250, 250, 250, 250])).to be false
      expect(AlertingService.shouldAlert?(threshold, [300,302,303,304,305])).to be false
      expect(AlertingService.shouldAlert?(threshold, [99,98,10,25,290])).to be false
    end

    it 'returns false when we have less values than expected' do
      threshold = build(:threshold)
      expect(AlertingService.shouldAlert?(threshold, [88, 89, 90])).to be false
    end
  end

  describe 'triggerAlert' do
    # Side effects only.  Nothing to test
  end

  describe 'sendAlertEmails' do
    # Side effects only.  Nothing to test
  end

end
