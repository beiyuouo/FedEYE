FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER jeecgos@163.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /jeecg-boot

WORKDIR /jeecg-boot

EXPOSE 8080

ARG JAR_NAME
ADD ./target/${JAR_NAME} ./platform.jar

CMD sleep 60;java -Djava.security.egd=file:/dev/./urandom -jar platform.jar

