class CreateTimezones < ActiveRecord::Migration[7.2]
  def change
    create_table :timezones do |t|
      t.string :name
      t.integer :offset

      t.timestamps
    end
    add_index :timezones, :name, unique: true
  end
end
