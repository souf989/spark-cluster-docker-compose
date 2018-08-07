package com.bnpparibas.monposte

import java.nio.file.{Path, Paths}

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object SampleApp extends App {

  val dataFolderPath = Paths.get("/mnt/data")

  def dataFilePath(dataType: String): Path = dataFolderPath.resolve(s"${dataType}.csv")

  val personsFilePath = dataFilePath("persons")
  val housesFilePath = dataFilePath("houses")
  val insultsFilePath = dataFilePath("insults")

  val joinedFilePath = dataFilePath("joined")

  def withSparkSession(appName: String)(block: SparkSession => Unit): Unit = {
    val sparkSession = SparkSession
        .builder()
        .appName("Example DataFrame")
      .getOrCreate()
    block(sparkSession)
    sparkSession.close()
  }

  def readDataFile(dataFilePath: Path)(implicit spark: SparkSession): DataFrame = {
    spark.read
        .option("header", "true")
      .csv(dataFilePath.toString)
      .repartition(5)
  }

  def writeDataFile(dataFrame: DataFrame, dataFilePath: Path)(implicit spark: SparkSession): Unit = {
    dataFrame.write.csv(dataFilePath.toString)
  }

  withSparkSession("sample-app") { implicit spark =>
    import spark.implicits._

    val personsDF = readDataFile(personsFilePath)
    val housesDF = readDataFile(housesFilePath)
    val insultsDF = readDataFile(insultsFilePath)

    val joinedDF = personsDF
      .join(housesDF, $"id" === $"person_id")
      .drop("person_id")
      .join(insultsDF, $"id" === $"person_id")
      .drop("person_id")

    writeDataFile(joinedDF, joinedFilePath)
  }



}
