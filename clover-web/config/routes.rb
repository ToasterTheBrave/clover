Rails.application.routes.draw do
  root 'reports#index'
  resources :alerts
  resources :users
  resources :thresholds
  resources :metrics
  resources :metric_sources
  resources :reports, only: [:index, :show]
  get 'reports/:id/generate', to: 'reports#generate', as: 'generate_report'
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
end
