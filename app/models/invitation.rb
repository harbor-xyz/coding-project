class Invitation < ApplicationRecord
  belongs_to :event
  belongs_to :user

  enum status: { pending: 0, accepted: 1, declined: 2 }

  validates :event, presence: true
  validates :user, presence: true
  validates :status, presence: true

  before_destroy :restore_availability_slot

  private

  def restore_availability_slot
    Availability.restore_slot(user, event.start_time, event.end_time)
  end
end
