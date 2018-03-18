FROM ruby:2.4.2-jessie

RUN apt-get update && \
    apt-get install -y \
      nodejs

ADD . /app/traffic-simulator

WORKDIR /app/traffic-simulator

# Update Bundler
RUN gem install bundler --pre

# Bundle install the gems
RUN bundler install
