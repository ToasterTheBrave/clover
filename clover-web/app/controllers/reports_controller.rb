class ReportsController < ApplicationController
  before_action :set_report, only: [:show, :edit, :update, :destroy]

  # GET /reports
  def index
    @reports = Report.all
  end

  # GET /reports/1
  def show
    @report_exists = File.file?("/home/truppert/projects/master-project/clover-web/public/reports/data/#{@report.timestamp}.js")
  end

  def generate
    set_report

    logger.info "/opt/spark/bin/spark-submit --class 'clover.service.BuildReport' /home/truppert/projects/master-project/clover-web/bin/clover.jar #{@report.timestamp}"
    system("/opt/spark/bin/spark-submit --class 'clover.service.BuildReport' /home/truppert/projects/master-project/clover-web/bin/clover.jar #{@report.timestamp}")

    success = false
    (1..10).each do |i|
      success = File.file?("/home/truppert/projects/master-project/clover-web/public/reports/data/#{@report.timestamp}.js")
      break if success
      sleep 5
    end

    if success
      redirect_to @report, notice: 'Report is generated'
    else
      redirect_to @report, notice: 'Report generation failed'
    end
  end


  private
    # Use callbacks to share common setup or constraints between actions.
    def set_report
      @report = Report.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def report_params
      params.require(:report)
    end
end
