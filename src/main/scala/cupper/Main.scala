package cupper

import scala.concurrent.{ExecutionContext, Future}
import scalajs.js
import facade.amazonaws.services.ec2._
import facade.amazonaws.services.cloudwatch._
import facade.amazonaws.AWSConfig
import facade.amazonaws.Error

import scala.scalajs.js.Object

object Main {
  def callback(err: Error, data: js.Any): Unit = {
    println(data)
    println(err.name)
    println(err.code)
    println(err.errors)
    println(err.region)
    println(err.requestId)
  }

  val REGION = "ap-northeast-1"
  val AWS_ACCESS_KEY = "xxx"
  val AWS_SECRET_KEY = "xxx"

  def main(args: Array[String]): Unit = {
    import js.JSConverters._

    implicit val ec = ExecutionContext.global

    println("start ...")

    val ec2 = new EC2(AWSConfig(
      region = REGION,
      apiVersion = "2016-11-15",
      accessKeyId = AWS_ACCESS_KEY,
      secretAccessKey = AWS_SECRET_KEY
    ))
    val params = DescribeInstancesRequest(
      Filters = scala.collection.mutable.Seq(
        Filter(
          Name = "tag:Name",
          Values = scala.collection.mutable.Seq("cupper").toJSArray
        )
      ).toJSArray
    )

    implicit val getMetricDateInput: DescribeInstancesResult => GetMetricDataInput =
      r => GetMetricDataInput(
        StartTime = new js.Date(System.currentTimeMillis() - 1000 * 3600),
        EndTime = new js.Date(System.currentTimeMillis()),
        MetricDataQueries = r.Reservations.get.flatMap(rl => rl.Instances.get).map(
          i => MetricDataQuery(
            Id = "q1",
            MetricStat = MetricStat(
              Metric = Metric(
                Namespace = "AWS/EC2",
                MetricName = "CPUUtilization",
                Dimensions = scala.collection.mutable.Seq(
                  Dimension(Name = "InstanceId", Value = i.InstanceId.get)
                ).toJSArray
              ),
              Stat = "Average",
              Period = 10
            )
          )
        ).toSeq.toJSArray
      )

    val cw = new CloudWatch(AWSConfig(
      region = REGION,
      accessKeyId = AWS_ACCESS_KEY,
      secretAccessKey = AWS_SECRET_KEY
    ))

    val temp: GetMetricDataInput => Future[String] = r => {
      println("getting cloud watch data")
      cw.getMetricDataFuture(r) andThen {
        case x => println(x)
      }
      Future("hogehoge")
    }

    for {
      r1 <- ec2.describeInstancesFuture(params)
//      r2 <- temp(r1)
      r2 <- cw.getMetricDataFuture(r1)
    } {
      r2.MetricDataResults.get.foreach(r => {
        println(r.Id)
        println(r.StatusCode)
        r.Values.get.foreach(println _)
      })
    }
  }
}

trait Params {
  val Filters: js.Array[js.Any]
}

trait Filters extends js.Any {
  val Name: String
  val Values: js.Array[String]
}
