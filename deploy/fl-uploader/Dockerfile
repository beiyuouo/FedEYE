FROM mcr.microsoft.com/java/jre:8u192-zulu-alpine

RUN adduser --disabled-password  --gecos ''  uploader
USER uploader
WORKDIR /data/projects/uploader

COPY target/FL-datauploader-1.0.0.jar ./
EXPOSE 8080
EXPOSE 8443

CMD java -Xmx2048m -Xms2048m -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -jar FL-datauploader-1.0.0.jar
