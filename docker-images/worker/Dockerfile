FROM spark-base:2.1.1

COPY ["./spark-env.sh.tpl", "/etc/spark/spark-env.sh.tpl"]

ENTRYPOINT ["/entrypoint"]
CMD ["start-worker"]
