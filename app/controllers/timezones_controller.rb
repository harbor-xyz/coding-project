class TimezonesController < ApplicationController
  before_action :set_timezone, only: [:show, :update, :destroy]

  def index
    @timezones = Timezone.all
    render json: @timezones
  end

  def show
    render json: @timezone
  end

  def create
    @timezone = Timezone.new(timezone_params)

    if @timezone.save
      render json: @timezone, status: :created
    else
      render json: @timezone.errors, status: :unprocessable_entity
    end
  end

  def update
    if @timezone.update(timezone_params)
      render json: @timezone, status: :ok
    else
      render json: @timezone.errors, status: :unprocessable_entity
    end
  end

  def destroy
    @timezone.destroy
    head :no_content
  end

  private

  def set_timezone
    @timezone = Timezone.find(params[:id])
  end

  def timezone_params
    params.require(:timezone).permit(:name, :offset)
  end
end
