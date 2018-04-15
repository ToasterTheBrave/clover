class MetricSourcesController < ApplicationController
  before_action :set_metric_source, only: [:show, :edit, :update, :destroy]

  # GET /metric_sources
  # GET /metric_sources.json
  def index
    @metric_sources = MetricSource.all
  end

  # GET /metric_sources/1
  # GET /metric_sources/1.json
  def show
  end

  # GET /metric_sources/new
  def new
    @metric_source = MetricSource.new
  end

  # GET /metric_sources/1/edit
  def edit
  end

  # POST /metric_sources
  # POST /metric_sources.json
  def create
    @metric_source = MetricSource.new(metric_source_params)

    respond_to do |format|
      if @metric_source.save
        format.html { redirect_to @metric_source, notice: 'Metric source was successfully created.' }
        format.json { render :show, status: :created, location: @metric_source }
      else
        format.html { render :new }
        format.json { render json: @metric_source.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /metric_sources/1
  # PATCH/PUT /metric_sources/1.json
  def update
    respond_to do |format|
      if @metric_source.update(metric_source_params)
        format.html { redirect_to @metric_source, notice: 'Metric source was successfully updated.' }
        format.json { render :show, status: :ok, location: @metric_source }
      else
        format.html { render :edit }
        format.json { render json: @metric_source.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /metric_sources/1
  # DELETE /metric_sources/1.json
  def destroy
    @metric_source.destroy
    respond_to do |format|
      format.html { redirect_to metric_sources_url, notice: 'Metric source was successfully destroyed.' }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_metric_source
      @metric_source = MetricSource.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def metric_source_params
      params.require(:metric_source).permit(:host, :port, :database)
    end
end
