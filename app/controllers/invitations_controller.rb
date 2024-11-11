class InvitationsController < ApplicationController
  before_action :set_invitation, only: [ :show, :update, :destroy ]
  before_action :set_event, only: [ :create ]
  before_action :set_user, only: [ :create ]

  def index
    @invitations = Invitation.all
    render json: @invitations
  end

  def show
    render json: @invitation
  end

  def create
    @availability = Availability.find_available_slot(@user, @event.start_time, @event.end_time)

    if @availability
      ActiveRecord::Base.transaction do
        # Create invitation
        @invitation = Invitation.new(invitation_params)

        if @invitation.save
          # Consume the time slot from availability
          @availability.consume_duration(@event.start_time, @event.end_time)
          render json: @invitation, status: :created
        else
          render json: @invitation.errors, status: :unprocessable_entity
        end
      end
    else
      render json: { error: "User is not available for this time slot" }, status: :unprocessable_entity
    end
  end

  def update
    if @invitation.update(invitation_params)
      render json: @invitation, status: :ok
    else
      render json: @invitation.errors, status: :unprocessable_entity
    end
  end

  def destroy
    @invitation.destroy
    head :no_content
  end

  private

  def set_invitation
    @invitation = Invitation.find(params[:id])
  end

  def set_event
    @event = Event.find(invitation_params[:event_id])
  end

  def set_user
    @user = User.find(invitation_params[:user_id])
  end

  def invitation_params
    params.require(:invitation).permit(:event_id, :user_id, :status)
  end
end
