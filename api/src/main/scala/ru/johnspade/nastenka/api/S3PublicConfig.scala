package ru.johnspade.nastenka.api

import zio.*

final case class S3PublicConfig(publicBucketUrl: String)

object S3PublicConfig:
  val descriptor: Config[S3PublicConfig] = Config.string("S3_PUBLIC_BUCKET_URL").map(url => S3PublicConfig(url))
