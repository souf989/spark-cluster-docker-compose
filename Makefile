SPARK_MASTER_CONTAINER = "spark_spark-master_1"



.PHONY: build-docker-images
build-docker-images:
	cd "./docker-images" ; \
	./build.sh

.PHONY: start-spark-cluster
start-spark-cluster:
	docker-compose \
		--project-name="spark" \
		--file="./docker-images/docker-compose.yml" \
		up

.PHONY: kill-spark-cluster
kill-spark-cluster:
	docker-compose \
		--project-name="spark" \
		--file="./docker-images/docker-compose.yml" \
		kill

./data/persons.csv:
	sudo cp "./persons.csv" "./data/persons.csv"

./data/houses.csv:
	sudo cp "./houses.csv" "./data/houses.csv"

./data/insults.csv:
	sudo cp "./insults.csv" "./data/insults.csv"

.PHONY: generate-data
generate-data: ./data/persons.csv ./data/houses.csv ./data/insults.csv

.PHONY: build-sample-app
build-sample-app:
	cd "./sample-app" ; \
	sbt assembly

.PHONY: copy-sample-app
copy-sample-app: build-sample-app
	cd "./sample-app"
	docker cp \
		"$(shell find "." -name "*.jar")" \
		"$(SPARK_MASTER_CONTAINER):/tmp/sample-app.jar"

.PHONY: run-sample-app
run-sample-app: copy-sample-app generate-data
	docker exec "$(SPARK_MASTER_CONTAINER)" \
		/usr/lib/spark/bin/spark-submit \
			--master "spark://spark-master:7077" \
			--class "com.bnpparibas.monposte.SampleApp" \
		"/tmp/sample-app.jar"


.PHONY: run-sample-app-hive
run-sample-app-hive: copy-sample-app
	docker exec "$(SPARK_MASTER_CONTAINER)" \
		/usr/lib/spark/bin/spark-submit \
			--master "spark://spark-master:7077" \
			--class "com.bnpparibas.monposte.SampleHiveApp" \
		"/tmp/sample-app.jar"

.PHONY: clean
clean:
	sudo rm -Rf ./data/*

.PHONY: demo
demo: clean run-sample-app-hive
