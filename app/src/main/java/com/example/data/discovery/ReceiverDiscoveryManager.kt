package com.example.data.discovery
import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetAddress
data class DiscoveredReceiver(val name:String,val host:String,val port:Int,val isC3Default:Boolean,val pingMs:Long?=null)
class ReceiverDiscoveryManager(private val context:Context){
 private val wifiManager=context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
 private var lock:WifiManager.MulticastLock?=null;private var job:Job?=null;private val scope=CoroutineScope(Dispatchers.IO)
 private val _items=MutableStateFlow<List<DiscoveredReceiver>>(emptyList());val discoveredReceivers:StateFlow<List<DiscoveredReceiver>>=_items.asStateFlow()
 private val _scanning=MutableStateFlow(false);val isScanning:StateFlow<Boolean>=_scanning.asStateFlow()
 fun startDiscovery(timeoutMs:Long=4000L){if(_scanning.value)return;_scanning.value=true;try{if(lock==null)lock=wifiManager?.createMulticastLock("C3MusicDiscoveryLock")?.apply{setReferenceCounted(false)};lock?.acquire()}catch(_:Exception){};job=scope.launch{try{val a=InetAddress.getByName("c3music.local");_items.value=listOf(DiscoveredReceiver("ESP32-C3",a.hostAddress?:"c3music.local",50005,true))}catch(_:Exception){};delay(timeoutMs);stopDiscovery()}}
 fun stopDiscovery(){if(!_scanning.value)return;_scanning.value=false;job?.cancel();job=null;try{if(lock?.isHeld==true)lock?.release()}catch(_:Exception){}}
}