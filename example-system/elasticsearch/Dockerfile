FROM docker.elastic.co/elasticsearch/elasticsearch-oss:6.1.1

RUN \
  wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/10.0.1+10/fb4372174a714e6b8c52526dc134031e/jre-10.0.1_linux-x64_bin.rpm && \
  wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/10.0.1+10/fb4372174a714e6b8c52526dc134031e/jdk-10.0.1_linux-x64_bin.rpm

RUN \
  yum install -y \
  jdk-10.0.1_linux-x64_bin.rpm \
  jdk-10.0.1_linux-x64_bin.rpm

RUN \
  rm jdk-10.0.1_linux-x64_bin.rpm \
     jre-10.0.1_linux-x64_bin.rpm

USER elasticsearch
WORKDIR /usr/share/elasticsearch
RUN \
  sed -e 's/^-Xms1g/-Xms512m/g' -i /usr/share/elasticsearch/config/jvm.options && \
  sed -e 's/^-Xmx1g/-Xmx512m/g' -i /usr/share/elasticsearch/config/jvm.options
