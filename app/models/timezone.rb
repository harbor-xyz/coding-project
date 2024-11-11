class Timezone < ApplicationRecord
  has_many :users
  has_many :availabilities
  has_many :events

  validates :name, presence: true, uniqueness: true
  validates :offset, presence: true
end
