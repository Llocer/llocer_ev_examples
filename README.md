# openEV examples by LLocer

The following examples are provided on this repository:

- OcppExample.java: Implementation of an OCPP websocket server using librarian [llocer_ocpp](https://github.com/Llocer/llocer_ocpp).
- ocpp_test1.py: python script to simulate an OCPP client.
- OcpiExample.java: Implementation of an OCPI node using librarian [llocer_ocpi](https://github.com/Llocer/llocer_ocpi).
 
Detailed descriptions are provided below.

## OCPP server

File OcppExample.java contains a basic implementation of an OCPP service. This service will handle Boot and Heartbeat messages, rejecting all remainder messages.

It implements the interface OcppAgent of the library llocer_ocpp. In this way we implement an Ocpp agent with identiy "CS1" that can be reached at URL http://.../cso/ocpp/CS1

** Implementation details **

OcppExample.java must implement the methods at interface OcppAgent:

- `String getId()` : returns a unique identifier of this agent instance in this OCPP server. The websocket url to access will be then http://.../cso/ocpp/<agent identifier>. In this example, an unique instance of the agent with hardcoded identifier "CS1" is used. The agent must be registered in the OCPP endpoint calling method `OcppEndpoint.putAgent` at server initialization (see file MainStartStop.java). 

- `void onOcppEndpointConnected( OcppEndpoint ocppEndpoint )` : called when a client connects (ocppEndpoint != null) or disconnects (ocppEndpoint == null). Usually, the OcppEndpoint is stored to allow sending of Ocpp Call messages.

- `Object onOcppCall( OcppAction action, Object payload )` : called when an Ocpp Call message is received. In this example, OcppActions BootNotification and Heartbeat are implemented, remainder messages are rejected with error.

- `void onOcppCallError( OcppCommand msg )` : called when an Ocpp Call Error is returned by the client. No body in this example.

- `void onOcppCallResult( OcppCommand command )` : called when an Ocpp Call Result is returned by the client. No body in this example.
 

** Install and run **

The jar with this element together with its dependencies must be loaded in your favorite websocket container (Tomcat, ...), probably packing it in a war file.

## simulated charging station

The provided script ocpp_test1.py is a test script that acts as a ChargingStation that connects to the OCPP server, sends a BootMessage and waits for the answer.

Before to use, modify line "server_url=" with the correct URL of your server.

Run as `./ocpp_test1.py` from command line. 

## OCPI node


## OCPI client

