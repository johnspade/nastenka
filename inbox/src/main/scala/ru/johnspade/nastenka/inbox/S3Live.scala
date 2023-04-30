package ru.johnspade.nastenka.inbox

import software.amazon.awssdk.regions.Region
import zio.*
import zio.s3.providers.*

import java.net.URI

object S3Live:
  val layer = ZLayer {
    ZIO
      .config(S3Config.descriptor)
      .map(config => zio.s3.liveZIO(Region.US_EAST_1, env, uriEndpoint = Some(URI(config.endpointUrl))))
  }.flatten
