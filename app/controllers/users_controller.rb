class UsersController < ApplicationController
  before_action :set_user, only: [ :availabilities, :set_availability, :find_overlap ]

  def index
    @users = User.all

    render json: @users
  end

  def show
    render json: @user
  end

  def create
    @user = User.new(user_params)

    if @user.save
      render json: @user, status: :created
    else
      render json: @user.errors, status: :unprocessable_entity
    end
  end

  def update
    if @user.update(user_params)
      render json: @user, status: :ok
    else
      render json: @user.errors, status: :unprocessable_entity
    end
  end

  def destroy
    @user.destroy
    head :no_content
  end

  def availabilities
    @availabilities = @user.availabilities
    render json: @availabilities
  end

  def set_availability
    @availability = @user.availabilities.build(availability_params)

    if @availability.save
      render json: @availability, status: :created
    else
      render json: @availability.errors, status: :unprocessable_entity
    end
  end

  def find_overlap
    @other_user = User.find_by(id: params[:other_user_id])

    if @user.nil? || @other_user.nil?
      render json: { error: "User not found" }, status: :not_found
      return
    end

    @overlapping_availabilities = Availability.find_overlapping_availabilities(@user.id, @other_user.id)
    render json: @overlapping_availabilities
  end

  private

  def set_user
    user_id = params[:id] || params[:user_id]
    @user = User.find_by(id: user_id)
  end

  def user_params
    params.require(:user).permit(:name, :email, :timezone_id)
  end

  def availability_params
    params.require(:availability).permit(:start_time, :end_time, :timezone_id)
  end
end
