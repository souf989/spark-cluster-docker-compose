#!/bin/bash

set -euo pipefail

build_base()
{
  cd "./base"
  docker build --no-cache -t "spark-base:2.1.1" "."
  cd -
}

build_master()
{
  docker-compose \
    --project-name="spark" \
    build \
      --no-cache "spark-master"
}

build_worker()
{
  docker-compose \
    --project-name="spark" \
    build \
      --no-cache "spark-worker"
}

build_history_server()
{
  docker-compose \
    --project-name="spark" \
    build \
      --no-cache "spark-history-server"
}


build_hive_server()
{
    current_branch="$(git rev-parse --abbrev-ref HEAD)"
	docker build -t bde2020/hive:${current_branch} ./

}

main()
{
  build_hive_server
  build_master
  build_worker
  build_history_server
}

main

