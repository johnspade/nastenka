package ru.johnspade.nastenka.email

import com.github.kklisura.cdt.launch.ChromeLauncher
import com.github.kklisura.cdt.protocol.commands.Page
import com.github.kklisura.cdt.protocol.types.page.PrintToPDF
import com.github.kklisura.cdt.protocol.types.page.PrintToPDFTransferMode
import com.github.kklisura.cdt.services.ChromeDevToolsService
import com.github.kklisura.cdt.services.ChromeService
import zio.*
import zio.nio.file.*

import java.util.Base64

trait PrintService:
  def print(html: String, outputFilename: String): ZIO[Any, Throwable, Unit]

final class PrintServiceLive(chromeService: ChromeService) extends PrintService:

  override def print(html: String, outputFilename: String): ZIO[Any, Throwable, Unit] =
    ZIO.scoped(for {
      tempFilePath    <- createTempHtmlFile(html)
      devToolsService <- createDevToolsService
      page            <- createPage(devToolsService)
      p               <- Promise.make[Throwable, Unit]
      _ <- ZIO.attemptBlocking {
        page.onLoadEventFired { event =>
          Runtime.default.unsafe.run(p.succeed(()))
        }
        page.navigate("file://" + tempFilePath.toString)
      }
      _          <- p.await
      printToPdf <- printToPdfA4(devToolsService)
      _          <- dump(outputFilename, printToPdf)
    } yield ())

  private def createTempHtmlFile(html: String) =
    for
      file <- Files.createTempFileScoped(suffix = ".html")
      _    <- Files.writeLines(file, List(html))
    yield file

  private def dump(filename: String, printToPdf: PrintToPDF) =
    val path = Path(filename)
    for
      out   <- Files.createFile(Path(filename))
      data  <- ZIO.attemptBlocking(printToPdf.getData)
      bytes <- ZIO.attempt(Base64.getDecoder().decode(data))
      _     <- Files.writeBytes(path, Chunk.fromArray(bytes))
    yield ()

  private val createDevToolsService =
    for
      tab <- ZIO.acquireRelease(
        ZIO.attemptBlocking(chromeService.createTab())
      ) { tab =>
        ZIO.attemptBlocking(chromeService.closeTab(tab)).orDie
      }
      devToolsService <- ZIO.acquireRelease(
        ZIO.attemptBlocking(chromeService.createDevToolsService(tab))
      ) { service =>
        ZIO.attemptBlocking {
          service.close()
        }.orDie
      }
    yield devToolsService

  private def createPage(devToolsService: ChromeDevToolsService) =
    ZIO.attemptBlocking {
      val page = devToolsService.getPage()
      page.enable()
      page
    }

  private def printToPdfA4(devToolsService: ChromeDevToolsService) =
    ZIO.attemptBlocking {
      val landscape               = false
      val displayHeaderFooter     = false
      val printBackground         = false
      val scale                   = 1d
      val paperWidth              = 8.27d // A4 paper format
      val paperHeight             = 11.7d // A4 paper format
      val marginTop               = 0d
      val marginBottom            = 0d
      val marginLeft              = 0d
      val marginRight             = 0d
      val pageRanges              = ""
      val ignoreInvalidPageRanges = false
      val headerTemplate          = ""
      val footerTemplate          = ""
      val preferCSSPageSize       = false
      val mode                    = PrintToPDFTransferMode.RETURN_AS_BASE_64
      devToolsService
        .getPage()
        .printToPDF(
          landscape,
          displayHeaderFooter,
          printBackground,
          scale,
          paperWidth,
          paperHeight,
          marginTop,
          marginBottom,
          marginLeft,
          marginRight,
          pageRanges,
          ignoreInvalidPageRanges,
          headerTemplate,
          footerTemplate,
          preferCSSPageSize,
          mode
        )
    }

end PrintServiceLive

object PrintServiceLive:
  val layer = ZLayer.fromFunction(new PrintServiceLive(_))
