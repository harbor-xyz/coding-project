class ChangeOffsetTypeInTimezones < ActiveRecord::Migration[7.2]
  def change
    change_column :timezones, :offset, :string
  end
end
