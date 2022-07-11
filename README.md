# openEV examples by LLocer

The following examples are provided on this repository:

OCPP related:
- OcppExample.java: Implementation of an OCPP websocket server using librarian [llocer_ocpp](https://github.com/Llocer/llocer_ocpp).
- ocpp_test1.py: python script to simulate an OCPP client.

OCPI related:
- OcpiTestLocationReceiver: Implementation of a OCPI node with the modules: Versions, Credentials and Locations (receiver interface) using librarian [llocer_ocpi](https://github.com/Llocer/llocer_ocpi).
- OcpiTestLocationSener: Implementation of a OCPI node with the modules: Versions, Credentials and Locations (sender interface) using same librarian.
 
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

## OCPI location receiver

File OcpiTestLocationReceiver.java contains the implementation of a OCPI node with the modules: Versions, Credentials and Locations (receiver interface).

This class extends the class OcpiServlet for OCPI node functionality and implements the interface OcpiLocationsReceiver to allow use of the module that provides the locations receiver interface. 

Following methods has been implemented to extend the OcpiServlet class:

- `OcpiVersions[] getVersions()` : must return the supported OCPI versions and their URL's.
- `OcpiEndpoints getEndpoints( String version )`: must return the supported interfaces for this protocol version.
- `OcpiLink authorizePeer( String authorization )`: must check if a request with the value of the token given in `authorization` parameter is allowed in this node. Usually, implemented with a single call to the module OcpiCredentialsModule.
- `OcpiResult<?> executeCredentials(OcpiRequestData oreq)`: execute a query to the credentials interface. Usually, implemented with a single call to the module OcpiCredentialsModule.
- `OcpiResult<?> execute( OcpiRequestData oreq )`: execute a http query to some module different from versions and credentials. Usually, field `oreq.module` is used to switch between call to the target modules.  

and following methods has been implemented to use the librarian module OcpiLocationsReceiver:

- `OcpiLocation getOcpiLocation( String id )`: retrive from local database the location with identifier `ìd`.
- `void updateLocation( OcpiLocation location, OcpiLocation delta )`: update the location `location` with the name received data `delta`
- `void updateEvse( OcpiLocation location, OcpiEvse evse, OcpiEvse delta )`: update the evse `evse` in location `location` with the name received data `delta` 
- `void updateConnector( OcpiLocation location, OcpiEvse evse, OcpiConnector connector, OcpiConnector delta )`: update the connection `connection` in the evse `evse` of location `location` with the name received data `delta`  



## OCPI location sender

File OcpiTestLocationSender.java contains the implementation of a OCPI servlet with the modules: Versions, Credentials and Locations (sender interface).

This class extends the class OcpiServlet for OCPI node functionality and implements the interface OcpiLocationsSender to allow use of the module that provides the locations sender interface.

See in previous chapter the details about extend class OcpiServlet. In order to use the module OcpiLocationsSender the following methods must be implemented:

- `OcpiLocation getOcpiLocation( String locationId )`: retrive from local database the location with identifier `ìd`.

and the methods necessary for pagination:

- `URI getOcpiModuleUri(InterfaceRole role, Identifier module)`: must return the public URI for the module.
- `int getOcpiPaginationLimit( Identifier module )`: must return the global limit for pagination.
- `Iterator<? extends HasLastUpdated> getOcpiItems(OcpiRequestData oreq)`: must return an iterator to available items (locations in this example).


