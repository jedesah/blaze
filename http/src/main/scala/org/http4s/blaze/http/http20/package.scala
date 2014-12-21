package org.http4s.blaze.http

import scala.util.control.NoStackTrace

package object http20 {
  
  val HeaderSize = 9

  private[http20] object Masks {
    val STREAMID = 0x7fffffff
    val LENGTH   =   0xffffff
    val int31    = 0x7fffffff

    val exclsive = ~int31
  }

  object FrameTypes {
    val DATA          = 0x0.toByte
    val HEADERS       = 0x1.toByte
    val PRIORITY      = 0x2.toByte
    val RST_STREAM    = 0x3.toByte
    val SETTINGS      = 0x4.toByte
    val PUSH_PROMISE  = 0x5.toByte
    val PING          = 0x6.toByte
    val GOAWAY        = 0x7.toByte
    val WINDOW_UPDATE = 0x8.toByte
    val CONTINUATION  = 0x9.toByte
  }

  type SettingKey = Short
  type SettingValue = Int
  case class Setting(key: SettingKey, value: SettingValue)

  object SettingsKeys {
    val SETTINGS_HEADER_TABLE_SIZE =      0x1.toShort
    val SETTINGS_ENABLE_PUSH =            0x2.toShort
    val SETTINGS_MAX_CONCURRENT_STREAMS = 0x3.toShort
    val SETTINGS_INITIAL_WINDOW_SIZE =    0x4.toShort
    val SETTINGS_MAX_FRAME_SIZE =         0x5.toShort
    val SETTINGS_MAX_HEADER_LIST_SIZE =   0x6.toShort
  }

  //////////////////////////////////////////////////

  sealed abstract class Http2Exception(val code: Int, msg: String) extends Exception(msg) with NoStackTrace

  case class NO_ERROR(msg: String)                                      extends Http2Exception(0x0, msg)
  case class PROTOCOL_ERROR(msg: String)                                extends Http2Exception(0x1, msg)
  case class INTERNAL_ERROR(msg: String)                                extends Http2Exception(0x2, msg)
  case class FLOW_CONTROL_ERROR(msg: String)                            extends Http2Exception(0x3, msg)
  case class SETTINGS_TIMEOUT(msg: String)                              extends Http2Exception(0x4, msg)
  case class STREAM_CLOSED(msg: String)                                 extends Http2Exception(0x5, msg)
  case class FRAME_SIZE_ERROR(msg: String, expected: Int, found: Int)   extends Http2Exception(0x6, msg)
  case class REFUSED_STREAM(id: Int)                                    extends Http2Exception(0x7, s"Stream $id refused")
  case class CANCEL(streamId: Int)                                      extends Http2Exception(0x8, "")
  case object COMPRESSION_ERROR                                         extends Http2Exception(0x9, "Compression error")
  case object CONNECT_ERROR                                             extends Http2Exception(0xa, "Connect Error")
  case object ENHANCE_YOUR_CALM                                         extends Http2Exception(0xb, "Enhance your calm")
  case object INADEQUATE_SECURITY                                       extends Http2Exception(0xc, "Inadequate security")
  case object HTTP_1_1_REQUIRED                                         extends Http2Exception(0xd, "HTTP/1.1 required")

  //////////////////////////////////////////////////

  sealed trait DecoderResult

  case object Success extends DecoderResult
  case object BufferUnderflow extends DecoderResult

  case class Error(err: Http2Exception) extends DecoderResult


  //////////////////////////////////////////////////

  private[http20] object Flags {
    val END_STREAM = 0x1.toByte
    def END_STREAM(flags: Byte): Boolean  = checkFlag(flags, END_STREAM)   // Data, Header

    val PADDED = 0x8.toByte
    def PADDED(flags: Byte): Boolean      = checkFlag(flags, PADDED)   // Data, Header

    val END_HEADERS = 0x4.toByte
    def END_HEADERS(flags: Byte): Boolean = checkFlag(flags, END_HEADERS)   // Header, push_promise

    val PRIORITY = 0x20.toByte
    def PRIORITY(flags: Byte): Boolean    = checkFlag(flags, PRIORITY)  // Header

    val ACK = 0x1.toByte
    def ACK(flags: Byte): Boolean         = checkFlag(flags, ACK)   // ping

    def DepID(id: Int): Int            = id & Masks.int31
    def DepExclusive(id: Int): Boolean = (Masks.exclsive & id) != 0
  }

  @inline
  private def checkFlag(flags: Byte, flag: Byte) = (flags & flag) != 0


}