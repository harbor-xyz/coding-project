# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `bin/rails
# db:schema:load`. When creating a new database, `bin/rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema[7.2].define(version: 2024_11_07_164325) do
  create_table "availabilities", force: :cascade do |t|
    t.datetime "start_time"
    t.datetime "end_time"
    t.integer "user_id", null: false
    t.integer "timezone_id", null: false
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["timezone_id"], name: "index_availabilities_on_timezone_id"
    t.index ["user_id", "start_time", "end_time"], name: "index_availabilities_on_user_id_and_times", unique: true
    t.index ["user_id"], name: "index_availabilities_on_user_id"
  end

  create_table "events", force: :cascade do |t|
    t.string "title"
    t.text "description"
    t.datetime "start_time"
    t.datetime "end_time"
    t.integer "timezone_id", null: false
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.string "organizer_name"
    t.string "organizer_email"
    t.integer "duration"
    t.index ["timezone_id"], name: "index_events_on_timezone_id"
  end

  create_table "invitations", force: :cascade do |t|
    t.integer "event_id", null: false
    t.integer "user_id", null: false
    t.integer "status"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["event_id"], name: "index_invitations_on_event_id"
    t.index ["user_id"], name: "index_invitations_on_user_id"
  end

  create_table "notifications", force: :cascade do |t|
    t.integer "user_id", null: false
    t.integer "notifiable_id", null: false
    t.string "notifiable_type"
    t.string "notification_type"
    t.datetime "read_at"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["notifiable_id"], name: "index_notifications_on_notifiable_id"
    t.index ["user_id"], name: "index_notifications_on_user_id"
  end

  create_table "timezones", force: :cascade do |t|
    t.string "name"
    t.string "offset"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["name"], name: "index_timezones_on_name", unique: true
  end

  create_table "users", force: :cascade do |t|
    t.string "name"
    t.string "email"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.integer "timezone_id", null: false
    t.index ["email"], name: "index_users_on_email", unique: true
  end

  add_foreign_key "availabilities", "timezones"
  add_foreign_key "availabilities", "users"
  add_foreign_key "events", "timezones"
  add_foreign_key "invitations", "events"
  add_foreign_key "invitations", "users"
  add_foreign_key "notifications", "notifiables"
  add_foreign_key "notifications", "users"
end
