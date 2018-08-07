package octo.example

import org.apache.spark._
import org.apache.spark.sql.SparkSession

case class Employee(firstName: String, lastName: String, companyId: Int)

object Employee {

  def sample: Seq[Employee] = Seq(
    Employee("Marc", "Alonso", 1),
    Employee("Adrien", "Besnard", 1),
    Employee("Solomon", "Hykes", 2),
    Employee("James", "Gosling", 3)
  )

}

case class Company(id: Int, name: String)

object Company {

  def sample: Seq[Company] = Seq(
    Company(1, "OCTO"),
    Company(2, "Docker"),
    Company(3, "Amazon Web Services")
  )

}

/*
SELECT
  firstName,
  lastName
FROM
  employees
JOIN
  companies
ON
  companyId = id
WHERE
  name = 'OCTO'

 */

object Show extends App {

  def withSparkContext(appName: String)(block: SparkContext => Unit): Unit = {
    val sparkConf = new SparkConf().setAppName(appName)
    val sparkContext = new SparkContext(sparkConf)
    block(sparkContext)
    sparkContext.stop()
  }

  def showRDD(): Unit = withSparkContext("Example RDD") { sparkContext =>
    val employeesRDD = sparkContext.parallelize(Employee.sample)
    val companiesRDD = sparkContext.parallelize(Company.sample)

    val employeesByCompanyIdRDD = employeesRDD
      .map({ e => (e.companyId, e) })

    val companiesByIdRDD = companiesRDD
      .map({ c => (c.id, c) })

    val octoEmployees = employeesByCompanyIdRDD.join(companiesByIdRDD)
      .map({ case (_, (e, c)) => (e, c) })
      .filter({ case (e, c) => c.name == "OCTO" })
      .map({ case (e, c) => (e.firstName, e.lastName) })

    separate("Result RDD") {
      octoEmployees.collect()
        .foreach(println)
    }
  }

  def withSparkSession(appName: String)(block: SparkSession => Unit): Unit = {
    val sparkSession = SparkSession
        .builder()
        .appName("Example DataFrame")
        .getOrCreate()
    block(sparkSession)
    sparkSession.close()
  }

  def showDataFrames(): Unit = withSparkSession("Example DataFrame") { sparkSession =>

    import sparkSession.implicits._

    val employeesDF = sparkSession.createDataFrame(Employee.sample)
    val companiesDF = sparkSession.createDataFrame(Company.sample)

    val resultDF = companiesDF
      .join(
        employeesDF,
        $"companyId" === $"id"
      )
    .select($"firstName", $"lastName")
    .where($"name" === "OCTO")

    separate("Result DataFrames") {
      resultDF.show()
    }
  }

  def separate(title: String)(block: => Unit): Unit = {
    println("=" * 20)
    println(s" --> ${title}: ")
    block
    println("=" * 20)
  }

  def showSQL(): Unit = withSparkSession("Example SQL") { sparkSession =>
    sparkSession.createDataFrame(Employee.sample).createOrReplaceTempView("employees")
    sparkSession.createDataFrame(Company.sample).createOrReplaceTempView("companies")

    val resultDF = sparkSession.sql(
      """
        |SELECT
        |  firstName,
        |  lastName
        |FROM
        |  employees
        |JOIN
        |  companies
        |ON
        |  companyId = id
        |WHERE
        |  name = 'OCTO'
      """.stripMargin)

    separate("Logical Plan") {
      val logicalPlan = resultDF.queryExecution.logical
      println(logicalPlan)
    }

    separate("Optimized Logical Plan") {
      val optimizedLogicalPlan = resultDF.queryExecution.optimizedPlan
      println(optimizedLogicalPlan)
    }


    separate("Physical Plan") {
      val physicalPlan = resultDF.queryExecution.executedPlan
      println(physicalPlan)
    }

    separate("Generated Code") {
      resultDF.queryExecution.debug.codegen()
    }

    separate("Result SQL") {
      resultDF.show()
    }

    separate("???") {
      resultDF.explain(true)
    }
  }

  showRDD()
  showDataFrames()
  showSQL()
}
