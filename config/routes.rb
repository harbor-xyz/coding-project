Rails.application.routes.draw do
  resources :users do
    member do
      get "availabilities"
      post "set_availability"
    end
    get "find_overlap/:other_user_id", to: "users#find_overlap", as: "find_overlap"
  end

  resources :timezones, only: [ :index ]

  resources :availabilities, except: [ :new, :edit ]

  post "schedule_meeting/:user_id", to: "events#schedule_meeting", as: "schedule_meeting"

  resources :events do
    member do
      post "invite"
    end
  end

  resources :invitations

  resources :notifications, only: [ :index, :update ]
end
