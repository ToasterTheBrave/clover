require 'test_helper'

class ThresholdsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @threshold = thresholds(:one)
  end

  test "should get index" do
    get thresholds_url
    assert_response :success
  end

  test "should get new" do
    get new_threshold_url
    assert_response :success
  end

  test "should create threshold" do
    assert_difference('Threshold.count') do
      post thresholds_url, params: { threshold: { count: @threshold.count, high_value: @threshold.high_value, interval: @threshold.interval, low_value: @threshold.low_value, metric_id: @threshold.metric_id } }
    end

    assert_redirected_to threshold_url(Threshold.last)
  end

  test "should show threshold" do
    get threshold_url(@threshold)
    assert_response :success
  end

  test "should get edit" do
    get edit_threshold_url(@threshold)
    assert_response :success
  end

  test "should update threshold" do
    patch threshold_url(@threshold), params: { threshold: { count: @threshold.count, high_value: @threshold.high_value, interval: @threshold.interval, low_value: @threshold.low_value, metric_id: @threshold.metric_id } }
    assert_redirected_to threshold_url(@threshold)
  end

  test "should destroy threshold" do
    assert_difference('Threshold.count', -1) do
      delete threshold_url(@threshold)
    end

    assert_redirected_to thresholds_url
  end
end
