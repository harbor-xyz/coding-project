class AddDurationToEvents < ActiveRecord::Migration[7.2]
  def change
    add_column :events, :duration, :integer
  end
end
