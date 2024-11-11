class RenameTimeZoneIdToTimezoneIdInUsers < ActiveRecord::Migration[7.2]
  def change
    remove_column :users, :time_zone_id
    add_column :users, :timezone_id, :integer, null: false
  end
end
