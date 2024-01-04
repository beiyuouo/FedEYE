FROM arm64v8/openjdk:8u191-jre-alpine3.9

WORKDIR /data/projects/uploader

COPY target/FL-datauploader-1.0.0.jar ./
EXPOSE 8080

CMD java -Xmx2048m -Xms2048m -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -jar FL-datauploader-1.0.0.jar
