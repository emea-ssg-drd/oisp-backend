FROM gradle:5.3.1-jdk
USER root
RUN apt update && apt install -y make

ADD ./build.gradle /app/build.gradle
#RUN cd /app && gradle build -x test --continue

ADD . /app
WORKDIR /app
RUN make build

FROM anapsix/alpine-java:8

RUN apk add make

COPY --from=0 /app /app
RUN rm -rf /app/build/distributions
WORKDIR /app

RUN adduser -D appuser
USER appuser

EXPOSE 8080