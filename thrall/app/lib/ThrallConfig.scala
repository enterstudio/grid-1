package lib

import com.amazonaws.services.ec2.{AmazonEC2, AmazonEC2ClientBuilder}
import com.gu.mediaservice.lib.config.CommonConfig
import com.gu.mediaservice.lib.elasticsearch.EC2
import play.api.Configuration

class ThrallConfig(override val configuration: Configuration) extends CommonConfig {
  private lazy val ec2Client: AmazonEC2 = withAWSCredentials(AmazonEC2ClientBuilder.standard()).build()

  final override lazy val appName = "thrall"

  lazy val queueUrl: String = properties("sqs.queue.url")

  lazy val imageBucket: String = properties("s3.image.bucket")

  lazy val writeAlias = properties.getOrElse("es.index.aliases.write", configuration.get[String]("es.index.aliases.write"))

  lazy val thumbnailBucket: String = properties("s3.thumb.bucket")

  lazy val elasticsearchHost: String =
    if (isDev)
      properties.getOrElse("es.host", "localhost")
    else
      EC2.findElasticsearchHost(ec2Client, Map(
        "Stage" -> Seq(stage),
        "Stack" -> Seq(elasticsearchStack),
        "App"   -> Seq(elasticsearchApp)
      ))


  lazy val elasticsearch6Host: String = properties.getOrElse("es6.host", "localhost")     // TODO how to discover the 6 cluster in AWS
  lazy val elasticsearch6Shards: Int = if (isDev) 1 else properties.getOrElse("es6.shards", "1").toInt
  lazy val elasticsearch6Replicas: Int = if (isDev) 0 else properties.getOrElse("es6.replicas", "0").toInt

  lazy val healthyMessageRate: Int = properties("sqs.message.min.frequency").toInt

  lazy val dynamoTopicArn: String = properties("indexed.image.sns.topic.arn")

  lazy val topicArn: String = properties("sns.topic.arn")
}
