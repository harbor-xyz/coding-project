class Event < ApplicationRecord
  belongs_to :timezone
  has_many :invitations, dependent: :destroy
  has_many :attendees, through: :invitations, source: :user

  validates :title, presence: true
  validates :start_time, presence: true
  validates :duration, presence: true, numericality: { greater_than: 0 }
  validate :end_time_after_start_time
  validates :organizer_name, presence: true
  validates :organizer_email, presence: true, format: { with: URI::MailTo::EMAIL_REGEXP }

  before_validation :set_end_time
  before_destroy :restore_availability_slots

  private

  def end_time_after_start_time
    errors.add(:end_time, "must be after start time") if end_time <= start_time
  end

  def set_end_time
    self.end_time = start_time + duration.minutes if start_time.present? && duration.present?
  end

  private

  def restore_availability_slots
    invitations.each do |invitation|
      Availability.restore_slot(invitation.user, start_time, end_time)
    end
  end
end
