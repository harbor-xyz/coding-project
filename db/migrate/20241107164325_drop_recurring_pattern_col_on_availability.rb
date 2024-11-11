class DropRecurringPatternColOnAvailability < ActiveRecord::Migration[7.2]
  def change
    remove_column :availabilities, :recurring_pattern
  end
end
