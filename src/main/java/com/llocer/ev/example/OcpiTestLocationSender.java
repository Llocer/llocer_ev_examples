package com.llocer.ev.example;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import com.llocer.collections.MemorySimpleMapFactory;
import com.llocer.common.SimpleMap;
import com.llocer.ev.ocpi.modules.OcpiCredentialsModule;
import com.llocer.ev.ocpi.modules.OcpiLocationsSenderModule;
import com.llocer.ev.ocpi.modules.OcpiLocationsSenderModule.OcpiLocationsSender;
import com.llocer.ev.ocpi.msgs.HasLastUpdated;
import com.llocer.ev.ocpi.msgs22.OcpiClientInfo;
import com.llocer.ev.ocpi.msgs22.OcpiCredentials;
import com.llocer.ev.ocpi.msgs22.OcpiCredentialsRole;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoint;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoint.Identifier;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoint.InterfaceRole;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoints;
import com.llocer.ev.ocpi.msgs22.OcpiLocation;
import com.llocer.ev.ocpi.msgs22.OcpiVersions;
import com.llocer.ev.ocpi.server.OcpiAgentId;
import com.llocer.ev.ocpi.server.OcpiConfig;
import com.llocer.ev.ocpi.server.OcpiLink;
import com.llocer.ev.ocpi.server.OcpiRequestBuilder;
import com.llocer.ev.ocpi.server.OcpiRequestData;
import com.llocer.ev.ocpi.server.OcpiResult;
import com.llocer.ev.ocpi.server.OcpiResult.OcpiResultEnum;
import com.llocer.ev.ocpi.server.OcpiServlet;

public class OcpiTestLocationSender extends OcpiServlet implements OcpiLocationsSender {

	private static final long serialVersionUID = -4764547316713059395L;
	
	// in order to skip URI.resolve problems, do not use initial slash and end by slash:
	private static final String servletPath = "ocpi/cpo/"; 
	private static final String servletPath221 = servletPath+"221/";

	private static final OcpiEndpoints endpoints = initEndpoints();

	private static final SimpleMap< String /*own token*/, OcpiLink > linksByToken = 
			MemorySimpleMapFactory.make( String.class, OcpiLink.class);
	private static final OcpiCredentialsModule ocpiCredentialsModule = new OcpiCredentialsModule(linksByToken); 
	
	private static OcpiTestLocationSender me = null; // only for testing
	

	private final SimpleMap<String /*locationId*/,OcpiLocation> locations;
	private final OcpiLocationsSenderModule locationsSenderModule;

	public OcpiTestLocationSender() {
		this.locations = MemorySimpleMapFactory.make( String.class, OcpiLocation.class );
		this.locationsSenderModule = new OcpiLocationsSenderModule( this );
		
		OcpiTestLocationSender.me = this;
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
	 * execution of modules and pagination
	 */
	
	@Override
	protected OcpiResult<?> execute( OcpiRequestData oreq ) throws Exception {
		switch( oreq.module ) {
			case LOCATIONS: return locationsSenderModule.senderInterface( oreq );
			default: return OcpiResultEnum.NOT_SUPPORTED_ENDPOINT;
		}
	}
	
	@Override
	public URI getOcpiModuleUri(InterfaceRole role, Identifier module) {
		return OcpiRequestBuilder.getModuleUrl(endpoints, module);
	}

	@Override
	public Iterator<? extends HasLastUpdated> getOcpiItems( OcpiRequestData oreq ) {
		switch( oreq.module ) {
		case LOCATIONS: 
			return this.locations.iterator();
			
		default:
			throw new IllegalArgumentException();
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
	 * implement OcpiLocationsSender
	 */
	
	@Override
	public OcpiLocation getOcpiLocation(String locationId) {
		return locations.get( locationId );
	}

	/*
	 * TEST: open link to OcppTestLocationReceiver node and send a location
	 */
	
	public static void test1() throws Exception {
		
		/*
		 * define link to OcppTestLocationReceiver node
		 */
		
		OcpiLink link = new OcpiLink();
		
		link.ownId = new OcpiAgentId( "ES", "CPO" );
		link.ownCredentials = new OcpiCredentials();
		link.ownCredentials.setUrl( OcpiConfig.getPublicURI().resolve( servletPath ) );
		
		OcpiCredentialsRole cpoRole = new OcpiCredentialsRole();
		cpoRole.setRole( OcpiClientInfo.Role.CPO ); 
		cpoRole.setCountryCode( link.ownId.countryCode );
		cpoRole.setPartyId( link.ownId.partyId );		
		link.ownCredentials.setRoles( Collections.singletonList( cpoRole ) );
		
		link.ownCredentials.setToken( OcpiCredentialsModule.makeRandomToken() );
		
		ocpiCredentialsModule.allowLink( link );
		
		link.peerCredentials = new OcpiCredentials();
		link.peerCredentials.setUrl( OcpiConfig.getPublicURI().resolve( OcpiTestLocationReceiver.servletPath ) );
		link.peerCredentials.setToken( OcpiTestLocationReceiver.initialToken );
		
		ocpiCredentialsModule.sendCredentials( link ); // send credentials to OcppTestLocationReceiver peer
		
		/*
		 * send a locations to the peer
		 */
		
		OcpiTestLocationSender.me.locationsSenderModule.addReceiver(link);
		
		OcpiLocation location = new OcpiLocation();
		location.setId( "myLocation" );
		location.setCountryCode( link.ownId.countryCode );
		location.setPartyId( link.ownId.partyId );
		location.setAddress( "Barrio sesamo");
		
		OcpiTestLocationSender.me.locationsSenderModule.reportLocationChange(location);
	}
	
}
