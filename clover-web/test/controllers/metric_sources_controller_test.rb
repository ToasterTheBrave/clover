require 'test_helper'

class MetricSourcesControllerTest < ActionDispatch::IntegrationTest
  setup do
    @metric_source = metric_sources(:one)
  end

  test "should get index" do
    get metric_sources_url
    assert_response :success
  end

  test "should get new" do
    get new_metric_source_url
    assert_response :success
  end

  test "should create metric_source" do
    assert_difference('MetricSource.count') do
      post metric_sources_url, params: { metric_source: { database: @metric_source.database, host: @metric_source.host, port: @metric_source.port } }
    end

    assert_redirected_to metric_source_url(MetricSource.last)
  end

  test "should show metric_source" do
    get metric_source_url(@metric_source)
    assert_response :success
  end

  test "should get edit" do
    get edit_metric_source_url(@metric_source)
    assert_response :success
  end

  test "should update metric_source" do
    patch metric_source_url(@metric_source), params: { metric_source: { database: @metric_source.database, host: @metric_source.host, port: @metric_source.port } }
    assert_redirected_to metric_source_url(@metric_source)
  end

  test "should destroy metric_source" do
    assert_difference('MetricSource.count', -1) do
      delete metric_source_url(@metric_source)
    end

    assert_redirected_to metric_sources_url
  end
end
