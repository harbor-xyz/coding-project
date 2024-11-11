class Notification < ApplicationRecord
  belongs_to :user
  belongs_to :notifiable

  enum notification_type: { invitation: 0, reminder: 1 }

  validates :user, presence: true
  validates :notifiable, presence: true
  validates :notification_type, presence: true
end
