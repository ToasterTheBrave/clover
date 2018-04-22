class AlertMailer < ApplicationMailer

  default from: 'notifications@example.com'

  def alert_email
    @threshold = params[:threshold]
    @alert = params[:alert]
    @metric = @threshold.metric

    subject = "Alert on #{@metric.measurement_name}:#{@metric.value_field}"

    @threshold.users.each do |user|
      puts "Emailing #{user.email}"
      mail(to: user.email, subject: subject)
    end
  end

end
