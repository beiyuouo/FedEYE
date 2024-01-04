# FedEYE Modules

## fl-frontend

environment: node.js=v18.15.0

build

```
npm install --registry=https://registry.npm.taobao.org
```

## fl-console-backend

environment: jdk=1.8.0_192

build

```
maven compile
```

run

```
java -Xmx2048m -Xms2048m -XX:+PrintGCDetails -XX:+PrintGCDateStamps
-Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -jar xxxx-1.0.0.jar
```

## fl-uploader

environment: jdk=1.8.0_192

build

```
maven compile
```

run

```
[java -Xmx2048m -Xms2048m
-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log
-XX:+HeapDumpOnOutOfMemoryError -jar FL-datauploader-1.0.0.jar ]()
```

## fledge-master

environment: go=1.16

build:

```
CGO_ENABLED=0 go build -ldflags="-w -s" -o fledge .
```

## License

Apache License 2.0
