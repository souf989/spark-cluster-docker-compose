FROM openjdk:8-jre-alpine

RUN apk --no-cache add --virtual build-dependencies \
      "wget" \
      "ca-certificates" \
      "tar" \
      "bash"

RUN mkdir "/tmp/spark" && \
    wget --no-verbose \
      "https://archive.apache.org/dist/spark/spark-2.1.1/spark-2.1.1-bin-hadoop2.6.tgz" \
      -O "/tmp/spark.tgz"

RUN mkdir "/usr/lib/spark" && \
    mkdir "/var/lib/spark" && \
    mkdir "/etc/spark" && \
    mkdir "/var/lib/spark/events"

RUN tar \
      -xzf "/tmp/spark.tgz" \
      -C "/usr/lib/spark" \
      --strip-components=1

RUN find "/usr/lib/spark" -name "*.cmd" | xargs -I {} rm '{}'

RUN wget --no-verbose "https://github.com/gliderlabs/sigil/releases/download/v0.4.0/sigil_0.4.0_$(uname -sm | tr \  _).tgz" -O "/tmp/sigil.tgz" && \
    tar \
      -xzf "/tmp/sigil.tgz" \
      -C "/usr/bin"

ENV PATH="${PATH}:/usr/lib/spark/bin:/usr/lib/spark/sbin"
RUN apk --no-cache add --virtual build-dependencies \
      "procps"

COPY ["./log4j.properties", "/etc/spark/log4j.properties"]

ENV SPARK_HOME="/usr/lib/spark"
ENV SPARK_CONF_DIR="/etc/spark"

COPY ["./entrypoint", "/entrypoint"]

COPY ["./spark-defaults.conf.tpl", "/etc/spark/spark-defaults.conf.tpl"]

ENTRYPOINT ["/entrypoint"]
