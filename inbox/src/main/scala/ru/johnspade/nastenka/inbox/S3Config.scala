package ru.johnspade.nastenka.inbox

import zio.config.ConfigDescriptor.*
import zio.config.*

final case class S3Config(bucketName: String, endpointUrl: String)

object S3Config:
  val descriptor = (string("S3_BUCKET_NAME") zip string("S3_ENDPOINT_URL")).to[S3Config]

  val live = ZConfig.fromSystemEnv(descriptor).orDie
