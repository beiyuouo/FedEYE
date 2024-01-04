FROM golang:1.16-alpine AS builder
RUN apk --no-cache add tzdata
RUN mkdir /app
ADD . /app
WORKDIR /app
RUN go mod tidy
RUN CGO_ENABLED=0 go build -ldflags="-w -s" -o fledge .

FROM alpine
COPY --from=builder /app/fledge .
COPY --from=builder /usr/share/zoneinfo /usr/share/zoneinfo
ENV TZ=Asia/Shanghai
ENTRYPOINT ["./fledge"]
