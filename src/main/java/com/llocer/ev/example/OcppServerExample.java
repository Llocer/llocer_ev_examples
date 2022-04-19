package com.llocer.ev.example;

import java.time.Instant;
import java.util.function.Consumer;

import com.llocer.ev.ocpp.msgs20.OcppBootNotificationResponse;
import com.llocer.ev.ocpp.msgs20.OcppHeartbeatResponse;
import com.llocer.ev.ocpp.msgs20.RegistrationStatusEnum;
import com.llocer.ev.ocpp.server.OcppAction;
import com.llocer.ev.ocpp.server.OcppAgent;
import com.llocer.ev.ocpp.server.OcppCommand;
import com.llocer.ev.ocpp.server.OcppEndpoint;
import com.llocer.ev.ocpp.server.OcppError;
import com.llocer.ev.ocpp.server.OcppMsg.OcppErrorCode;

public class OcppServerExample implements OcppAgent {
	
	private OcppEndpoint ocppEndpoint = null;
	
	static {
		OcppServerExample it = new OcppServerExample();
		OcppEndpoint.putAgent( it );
	}

	@Override
	public String getId() {
		return "CS1";
	}
	
	/******************************************************************************
	 * OCPP messages
	 */
	
	public void send( Object payload, Consumer<OcppCommand> callback ) {
		this.ocppEndpoint.sendAction( payload, callback );
	}

	@Override
	public void onOcppEndpointConnected( OcppEndpoint ocppEndpoint ) {
		// ocppEndpoint == null => disconnected
		this.ocppEndpoint = ocppEndpoint;
	}
	
	@Override
	public Object onOcppCall(OcppAction action, Object payload) throws Exception {
		switch( action ) {
		case BootNotification: {
//			OcppBootNotificationRequest bootNotification  = (OcppBootNotificationRequest)payload;
			
			OcppBootNotificationResponse response = new OcppBootNotificationResponse();
			response.setCurrentTime( Instant.now() );
			response.setStatusInfo( null );
			response.setStatus( RegistrationStatusEnum.ACCEPTED );
			response.setInterval( OcppEndpoint.config.heartbeatInterval );
			return response;
		}

		case Heartbeat: {
			OcppHeartbeatResponse response = new OcppHeartbeatResponse();
			response.setCurrentTime( Instant.now() );
			return response;
		}
		
		default:
			return new OcppError( OcppErrorCode.MessageTypeNotSupported, "" );
		} 
	}

	@Override
	public void onOcppCallResult( OcppCommand command ) {
	}

	@Override
	public void onOcppCallError( OcppCommand command ) {
	}
}
