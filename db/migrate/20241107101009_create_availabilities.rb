class CreateAvailabilities < ActiveRecord::Migration[7.2]
  def change
    create_table :availabilities do |t|
      t.datetime :start_time
      t.datetime :end_time
      t.text :recurring_pattern
      t.references :user, null: false, foreign_key: true
      t.references :timezone, null: false, foreign_key: true

      t.timestamps
    end
    add_index :availabilities, [:user_id, :start_time, :end_time], unique: true, name: 'index_availabilities_on_user_id_and_times'
  end
end
