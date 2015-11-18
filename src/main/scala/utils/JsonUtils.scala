package utils

import com.typesafe.scalalogging.LazyLogging
import messages.ErrorMessage
import net.liftweb.json.{DefaultFormats, _}

import scala.util.{Failure, Success, Try}

object JsonUtils extends App with LazyLogging {

  def toObject[T](json: String)(implicit m: Manifest[T]): Either[ErrorMessage, T] = {
    implicit val jsonFormats = DefaultFormats
//    val jsons = "{\"username\":\"Simer\",\"name\":\"Simer\",\"password\":\"123\"}"
    Try(
      parse(json).extract[T]
    ) match {
      case Failure(exception) =>
        val errorMessage = "Error parsing to JSON"
        logger.error(errorMessage, exception)
        Left(ErrorMessage(errorMessage))
      case Success(jsonObject) =>
        Right(jsonObject)
    }
  }
}


