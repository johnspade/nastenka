package ru.johnspade.nastenka.inbox

import zio.Config
import zio.config.*

import Config.*

final case class S3Config(bucketName: String, endpointUrl: String)

object S3Config:
  val descriptor = (string("S3_BUCKET_NAME") zip string("S3_ENDPOINT_URL")).to[S3Config]
