package com.example.data.network
import android.util.Log
import com.example.model.AudioStreamFormat
import com.example.model.StreamingState
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
class UdpStreamClient(private val onStateChanged:(StreamingState,String?)->Unit,private val onBytesTransmitted:(Long)->Unit){
 private var socket:DatagramSocket?=null;private var address:InetAddress?=null;private var port=50005;private val running=AtomicBoolean(false);private val stopped=AtomicBoolean(false);val totalBytesWritten=AtomicLong(0L)
 @Synchronized fun connectAndStart(host:String,port:Int,format:AudioStreamFormat):Boolean{stopped.set(false);onStateChanged(StreamingState.CONNECTING,null);if(!C3Protocol.isSupportedC3UdpFormat(format)){val e="Unsupported C3 UDP format: "+format.displayName;onStateChanged(StreamingState.ERROR,e);return false};return try{val a=InetAddress.getByName(host);val s=DatagramSocket();s.connect(a,port);address=a;this.port=port;socket=s;sendDatagram(C3Protocol.createC3Header(format));running.set(true);onStateChanged(StreamingState.STREAMING,a.hostAddress);Log.i("UdpStreamClient","UDP streaming to "+a.hostAddress+":"+port);true}catch(e:Exception){closeSocket();val msg="UDP setup failed: "+e.message;Log.e("UdpStreamClient",msg);onStateChanged(StreamingState.ERROR,msg);false}}
 @Synchronized fun sendAudioChunk(buffer:ByteArray,offset:Int,length:Int):Boolean{if(!running.get()||stopped.get())return false;return try{var sent=0;while(sent<length){val n=minOf(1456,length-sent);val q=DatagramPacket(buffer,offset+sent,n,address,port);socket?.send(q)?:return false;sent+=n;val total=totalBytesWritten.addAndGet(n.toLong());onBytesTransmitted(total)};true}catch(e:Exception){if(!stopped.get()){val msg="UDP transmission error: "+e.message;Log.w("UdpStreamClient",msg);closeSocket();onStateChanged(StreamingState.DISCONNECTED,msg)};false}}
 @Synchronized fun disconnect(isManual:Boolean=true){stopped.set(isManual);closeSocket();if(isManual)onStateChanged(StreamingState.IDLE,null)}
 private fun sendDatagram(data:ByteArray){val q=DatagramPacket(data,data.size,address,port);socket?.send(q)?:error("UDP socket unavailable");onBytesTransmitted(totalBytesWritten.addAndGet(data.size.toLong()))}
 private fun closeSocket(){running.set(false);try{socket?.close()}catch(_:Exception){};socket=null;address=null}
}