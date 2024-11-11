class Availability < ApplicationRecord
  belongs_to :user
  belongs_to :timezone

  validates :start_time, presence: true
  validates :end_time, presence: true
  validate :end_time_after_start_time

  def self.find_overlapping_availabilities(user1_id, user2_id)
    Availability
      .where(user_id: user1_id)
      .joins("INNER JOIN availabilities AS other_avail ON other_avail.user_id = #{user2_id}")
      .where("availabilities.start_time < other_avail.end_time AND availabilities.end_time > other_avail.start_time")
      .select(
        "CASE WHEN availabilities.start_time > other_avail.start_time THEN availabilities.start_time ELSE other_avail.start_time END AS start_time",
        "CASE WHEN availabilities.end_time < other_avail.end_time THEN availabilities.end_time ELSE other_avail.end_time END AS end_time"
      )
      .map { |avail| { start_time: avail.start_time, end_time: avail.end_time } }
  end

  def self.find_available_slot(user, start_time, end_time)
    # First find a potential availability slot
    availability = user.availabilities
    .where("start_time <= ? AND end_time >= ?", end_time, start_time)
    .first

    return nil unless availability

    # Check if there are any existing events that would overlap
    has_overlapping_events = Event.joins(:invitations)
    .where(invitations: { user_id: user.id })
    .where("start_time < ? AND end_time > ?", end_time, start_time)
    .exists?

    has_overlapping_events ? nil : availability
  end

  def consume_duration(start_time, end_time)
    if self.start_time < start_time && self.end_time > end_time
      # case of booking in middle of the availability
      remaining_start = self.dup
      remaining_start.end_time = start_time
      remaining_start.save!

      remaining_end = self.dup
      remaining_end.start_time = end_time
      remaining_end.save!

      self.destroy
    elsif self.start_time >= start_time && self.end_time <= end_time
      # The entire availability is consumed, no need to split
      self.destroy
    else
      # Partial overlap, adjust the start_time or end_time accordingly
      if self.start_time < start_time
        self.update!(end_time: start_time)
      else
        self.update!(start_time: end_time)
      end
    end
  end

  def self.restore_slot(user, start_time, end_time)
    return if user.availabilities.exists?(start_time: start_time, end_time: end_time)
    # Find adjacent or overlapping availabilities
    adjacent_slots = user.availabilities
      .where("(end_time = ? OR start_time = ?)", start_time, end_time)
      .order(:start_time)

    if adjacent_slots.empty?
      # Create new availability if no adjacent slots
      user.availabilities.create!(
        start_time: start_time,
        end_time: end_time,
        timezone: user.timezone
      )
    else
      adjacent_slots.each do |slot|
        if slot.end_time == start_time
          # Extend the existing slot's end time
          slot.update!(end_time: end_time)
        elsif slot.start_time == end_time
          # Extend the existing slot's start time
          slot.update!(start_time: start_time)
        end
      end
    end
  end

  def end_time_after_start_time
    errors.add(:end_time, "must be after start time") if end_time <= start_time
  end
end
