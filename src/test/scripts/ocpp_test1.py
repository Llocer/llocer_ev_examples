#!/usr/bin/python3

#server_url="ws://127.0.0.1:8080/llocer_cso_war/example/ocpp/RDAM%20123"
server_url="ws://127.0.0.1:8080/llocer_cso_war/cso/ocpp/CS1"

import websocket # pip3 install websocket-client
import json
from time import sleep, time
import ssl
import uuid

def log( msg ):
    print("\n%d %s"%(time()%100000,msg))

def no_mask( s ): 
    return b'\0\0\0\0'

ws = websocket.WebSocket( sslopt={"cert_reqs": ssl.CERT_NONE}, get_mask_key=no_mask )
ws.connect( server_url )

def send( msg ):
    global ws
    js = json.dumps( msg )
    log( "<<< %s ..."%js )
    ws.send( js )

def recv():
    global msg
    log( "waiting msg ..." )
    js = ws.recv()

    if not js:
        log( "empty message, ending." )
        exit(0)

    log( ">>> %s"%js )
    msg = json.loads( js )
    return msg

send( [ 2, str(uuid.uuid4()), "BootNotification", 
{ "reason": "PowerUp",
    "chargingStation": {
 	"model": "pulsar",
    	"vendorName": "wallbox"
      } 
} ] )
    
recv() # boot
