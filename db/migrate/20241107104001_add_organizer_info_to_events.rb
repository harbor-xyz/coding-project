class AddOrganizerInfoToEvents < ActiveRecord::Migration[7.2]
  def change
    add_column :events, :organizer_name, :string
    add_column :events, :organizer_email, :string
  end
end
