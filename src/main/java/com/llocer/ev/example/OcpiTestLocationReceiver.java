package com.llocer.ev.example;

import java.util.Collections;
import java.util.LinkedList;

import com.llocer.collections.MemorySimpleMapFactory;
import com.llocer.common.Log;
import com.llocer.common.SimpleMap;
import com.llocer.ev.ocpi.modules.OcpiCredentialsModule;
import com.llocer.ev.ocpi.modules.OcpiLocationsReceiverModule;
import com.llocer.ev.ocpi.modules.OcpiLocationsReceiverModule.OcpiLocationsReceiver;
import com.llocer.ev.ocpi.msgs22.OcpiClientInfo;
import com.llocer.ev.ocpi.msgs22.OcpiConnector;
import com.llocer.ev.ocpi.msgs22.OcpiCredentials;
import com.llocer.ev.ocpi.msgs22.OcpiCredentialsRole;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoint;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoints;
import com.llocer.ev.ocpi.msgs22.OcpiEvse;
import com.llocer.ev.ocpi.msgs22.OcpiLocation;
import com.llocer.ev.ocpi.msgs22.OcpiVersions;
import com.llocer.ev.ocpi.server.OcpiAgentId;
import com.llocer.ev.ocpi.server.OcpiConfig;
import com.llocer.ev.ocpi.server.OcpiLink;
import com.llocer.ev.ocpi.server.OcpiRequestData;
import com.llocer.ev.ocpi.server.OcpiResult;
import com.llocer.ev.ocpi.server.OcpiResult.OcpiResultEnum;
import com.llocer.ev.ocpi.server.OcpiServlet;

public class OcpiTestLocationReceiver extends OcpiServlet implements OcpiLocationsReceiver {

	private static final long serialVersionUID = -4764547316713059395L;
	
	// in order to skip URI.resolve problems, do not use initial slash and end by slash:
	static final String servletPath = "ocpi/eMSP/"; 
	private static final String servletPath221 = servletPath+"221/";
	static final String initialToken = "Token TMP";

	private static final OcpiEndpoints endpoints = initEndpoints();

	private static final SimpleMap< String /*own token*/, OcpiLink > linksByToken = 
			MemorySimpleMapFactory.make( String.class, OcpiLink.class);
	private static final OcpiCredentialsModule ocpiCredentialsModule = new OcpiCredentialsModule(linksByToken); 
	
	private final SimpleMap<String /*locationId*/,OcpiLocation> locations;
	private final OcpiLocationsReceiverModule locationsReceiverModule;

	public OcpiTestLocationReceiver() {
		this.locations = MemorySimpleMapFactory.make( String.class, OcpiLocation.class );
		this.locationsReceiverModule = new OcpiLocationsReceiverModule( this );
	}
	
	/*
	 * versions and endpoints
	 */

	@Override
	protected OcpiVersions[] getVersions() {
		OcpiVersions answer = new OcpiVersions();
		answer.setVersion( OcpiEndpoints.Version._2_2_1 );
		answer.setUrl( OcpiConfig.getPublicURI().resolve( servletPath221 ) );
		return new OcpiVersions[] { answer };
	}

	private static OcpiEndpoints initEndpoints() {
		OcpiEndpoints endpoints = new OcpiEndpoints();
		endpoints.setVersion( OcpiEndpoints.Version._2_2_1 );
		endpoints.setEndpoints( new LinkedList<OcpiEndpoint>() );
		
		OcpiEndpoint endpoint;

		endpoint = new OcpiEndpoint();
		endpoint.setIdentifier( OcpiEndpoint.Identifier.CREDENTIALS );
		endpoint.setRole( OcpiEndpoint.InterfaceRole.SENDER );
		endpoints.getEndpoints().add( endpoint );

		endpoint = new OcpiEndpoint();
		endpoint.setIdentifier( OcpiEndpoint.Identifier.CREDENTIALS );
		endpoint.setRole( OcpiEndpoint.InterfaceRole.RECEIVER );
		endpoints.getEndpoints().add( endpoint );

		endpoint = new OcpiEndpoint();
		endpoint.setIdentifier( OcpiEndpoint.Identifier.LOCATIONS );
		endpoint.setRole( OcpiEndpoint.InterfaceRole.SENDER );
		endpoints.getEndpoints().add( endpoint );

		for( OcpiEndpoint e : endpoints.getEndpoints() ) {
			e.setUrl( OcpiConfig.getPublicURI()
					.resolve( servletPath221+e.getIdentifier().toString()+"/" ));
		}
		
		return endpoints;
	}
	
	@Override
	protected OcpiEndpoints getEndpoints( String version ) {
		return endpoints;
	}

	/*
	 * links
	 */
	
	public static OcpiLink makeLink() {
		OcpiLink link = new OcpiLink();
		
		link.ownId = new OcpiAgentId( "ES", "MSP" );
		link.ownCredentials = new OcpiCredentials();
		link.ownCredentials.setUrl( OcpiConfig.getPublicURI().resolve( servletPath ) );
		
		OcpiCredentialsRole emspRole = new OcpiCredentialsRole();
		emspRole.setRole( OcpiClientInfo.Role.EMSP ); 
		emspRole.setCountryCode( link.ownId.countryCode );
		emspRole.setPartyId( link.ownId.partyId );		
		link.ownCredentials.setRoles( Collections.singletonList( emspRole ) );
		
		link.ownCredentials.setToken( initialToken );
		
		ocpiCredentialsModule.allowLink( link );

		return link;
	}
	
	/*
	 * execution of modules and pagination
	 */
	
	@Override
	protected OcpiResult<?> execute( OcpiRequestData oreq ) throws Exception {
		switch( oreq.module ) {
			case LOCATIONS: return locationsReceiverModule.receiverInterface( oreq );
			default: return OcpiResultEnum.NOT_SUPPORTED_ENDPOINT;
		}
	}
	
	/*
	 * links & credentials
	 */
	
	@Override
	protected OcpiLink authorizePeer(String authorization) {
		return ocpiCredentialsModule.authorizePeer(authorization);
	}
	
	@Override
	protected OcpiResult<?> executeCredentials( OcpiRequestData oreq ) throws Exception {
		return ocpiCredentialsModule.commonInterface(oreq);
	}

	/*
	 * implement locations receiver
	 */
	
	@Override
	public OcpiLocation getOcpiLocation(String locationId) {
		return locations.get( locationId );
	}

	@Override
	public void updateLocation(OcpiLocation location, OcpiLocation delta) {
		Log.debug( "updateLocation: %s", location );
	}

	@Override
	public void updateEvse(OcpiLocation location, OcpiEvse evse, OcpiEvse delta) {
		Log.debug( "updateEvse: %s", evse );
	}

	@Override
	public void updateConnector(OcpiLocation location, OcpiEvse evse, OcpiConnector connector, OcpiConnector delta) {
		Log.debug( "updateConnector: %s", connector );
	}
}
