package com.example.data.network
import com.example.model.AudioStreamFormat
import java.nio.ByteBuffer
import java.nio.ByteOrder
object C3Protocol {
 const val DEFAULT_RECEIVER_HOSTNAME="c3music.local"; const val DEFAULT_UDP_PORT=50005; const val S3_RECEIVER_HOSTNAME="s3music.local"; const val S3_RECEIVER_UDP_PORT=50005
 val C3_MAGIC=byteArrayOf(0x43,0x33,0x4D,0x53); const val C3_PROTOCOL_VERSION:Byte=0x01; const val C3_HEADER_BYTES=16
 fun createC3Header(format:AudioStreamFormat):ByteArray{val b=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);b.put(C3_MAGIC);b.put(C3_PROTOCOL_VERSION);b.put(format.bitDepth.toByte());b.put(format.channelCount.toByte());b.put(0);b.putInt(format.sampleRate);b.putInt(format.frameSizeBytes);return b.array()}
 fun isSupportedC3UdpFormat(format:AudioStreamFormat)=format.channelCount==2&&format.bitDepth==16&&(format.sampleRate==44100||format.sampleRate==48000)
}