package messages

trait Response

trait Command extends Response

case class Acknowledge(id: String) extends Response

case class ErrorMessage(message: String, state: Option[State] = None) extends Response

case class Validation(message: String) extends Response

case class Timeout(message: String = "Ops! Request timed out.") extends Response

case class Message(message: String, state: Option[State] = None) extends Response

trait State extends Response

trait Event extends Response
