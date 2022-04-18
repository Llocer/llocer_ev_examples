# llocer_ev_examples

Examples of implementation of OCPP and OCPI services.

## OCPP server

File OcppExample.java contains a basic implementation of an OCPP service. This service will handle Boot and Heartbeat messages, rejecting all remainder messages.

It implements the interface OcppAgent of the library llocer_ocpp. In this way we implement an Ocpp agent with identiy "CS1" that can be reached at URL http://.../cso/ocpp/CS1

The provided script ocpp_test1.py is a python script that acts as a ChargingStation connecting to this OCPP server, sends to it a BootMessage and waits for the answer.


