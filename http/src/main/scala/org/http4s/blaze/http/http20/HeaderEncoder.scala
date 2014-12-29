package org.http4s.blaze.http.http20

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.US_ASCII


import com.twitter.hpack.Encoder

/** This needs to contain the state of the header Encoder */
trait HeaderEncoder[T] {

  /** Note that the default value is 4096 bytes */
  def setMaxTableSize(max: Int): Unit

  def encodeHeaders(hs: T, done: Boolean): ByteBuffer
}

/** Simple Headers type for use in blaze and testing */
final class SeqTupleHeaderEncoder(initialMaxTableSize: Int) extends HeaderEncoder[Seq[(String, String)]] {

  private val encoder = new Encoder(initialMaxTableSize)
  private val os = new ByteArrayOutputStream(512)

  override def setMaxTableSize(max: Int): Unit = encoder.setMaxHeaderTableSize(os, max)

  override def encodeHeaders(hs: Seq[(String, String)], done: Boolean): ByteBuffer = {
    hs.foreach { case (k,v) => encoder.encodeHeader(os, k.getBytes(US_ASCII), v.getBytes(US_ASCII), false) }
    val buff = ByteBuffer.wrap(os.toByteArray())
    os.reset()
    buff
  }
}
