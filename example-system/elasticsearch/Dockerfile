FROM docker.elastic.co/elasticsearch/elasticsearch-oss:6.1.1

USER elasticsearch
WORKDIR /usr/share/elasticsearch
RUN \
  sed -e 's/^-Xms1g/-Xms512m/g' -i /usr/share/elasticsearch/config/jvm.options && \
  sed -e 's/^-Xmx1g/-Xmx512m/g' -i /usr/share/elasticsearch/config/jvm.options
