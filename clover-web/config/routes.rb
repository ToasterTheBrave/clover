Rails.application.routes.draw do
  resources :thresholds
  resources :metrics
  resources :metric_sources
  resources :reports, only: [:index, :show]
  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
end
