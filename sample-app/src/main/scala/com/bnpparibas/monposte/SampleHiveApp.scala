package com.bnpparibas.monposte


import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}


object SampleHiveApp extends App {

  def withSparkSession(appName: String)(block: SparkSession => Unit): Unit = {

    val spark = SparkSession
      .builder()
      .appName("app to test hive connexion")
      .config("hive.metastore.uris", "thrift://hive-metastore:9083")
      .enableHiveSupport()
      .getOrCreate()

    block(spark)
    spark.close()
  }



  import org.apache.hadoop.conf.Configuration
  import org.apache.hadoop.fs._

  def merge(srcPath: String, dstPath: String): Unit =  {
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath), true, hadoopConfig, null)
    // the "true" setting deletes the source files once they are merged into the new output
  }

  withSparkSession("sample-hive-app") { implicit spark =>
    import spark.implicits._
    import org.apache.spark.sql.hive.HiveContext

   val df = spark.sql("select * from pokes limit 5")


    df.write
      .mode("overwrite")
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .save("hdfs://namenode:8020/tmp/part/out_part.csv")


    merge("hdfs://namenode:8020/tmp/part/out_part.csv", "hdfs://namenode:8020/tmp/merged/out_merged.csv" )
  }




}
