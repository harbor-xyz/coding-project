class User < ApplicationRecord
  belongs_to :timezone, class_name: "Timezone"

  has_many :availabilities, dependent: :destroy
  has_many :invitations, dependent: :destroy
  has_many :notifications, dependent: :destroy

  validates :name, presence: true
  validates :email, presence: true, uniqueness: true
end
