class EventsController < ApplicationController
  before_action :set_event, only: [:show, :update, :destroy, :invite]

  def index
    @events = Event.all
    render json: @events
  end

  def show
    render json: @event
  end

  def create
    @event = Event.new(event_params)

    if @event.save
      render json: @event, status: :created
    else
      render json: @event.errors, status: :unprocessable_entity
    end
  end

  def update
    if @event.update(event_params)
      render json: @event, status: :ok
    else
      render json: @event.errors, status: :unprocessable_entity
    end
  end

  def destroy
    @event.destroy
    head :no_content
  end

  def invite
    @user = User.find(params[:user_id])
    @invitation = @event.invitations.build(user: @user, status: :pending)

    if @invitation.save
      render json: @invitation, status: :created
    else
      render json: @invitation.errors, status: :unprocessable_entity
    end
  end

  def schedule_meeting
    @user = User.find(params[:user_id])
    start_time = params[:start_time].to_datetime
    end_time = start_time + params[:duration].minutes
    @availability = Availability.find_available_slot(@user, start_time, end_time)

    if @availability
      @event = Event.new(
        title: params[:title],
        description: params[:description],
        start_time: start_time,
        duration: params[:duration],
        timezone: @availability.timezone,
        organizer_name: params[:organizer_name],
        organizer_email: params[:organizer_email]
      )

      if @event.save
        @invitation = @event.invitations.build(user: @user, status: :accepted)
        @invitation.save!

        @availability.consume_duration(@event.start_time, @event.end_time)

        render json: @event, status: :created
      else
        render json: @event.errors, status: :unprocessable_entity
      end
    else
      render json: { error: "No available slot found for the requested time and duration" }, status: :unprocessable_entity
    end
  end

  private

  def set_event
    @event = Event.find(params[:id])
  end

  def event_params
    params.require(:event).permit(:title, :description, :start_time, :duration, :timezone_id, :organizer_name, :organizer_email)
  end
end
