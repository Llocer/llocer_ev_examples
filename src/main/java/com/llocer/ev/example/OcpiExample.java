package com.llocer.ev.example;

import java.util.Collections;
import java.util.LinkedList;

import com.llocer.collections.MemorySimpleMapFactory;
import com.llocer.common.SimpleMap;
import com.llocer.ev.ocpi.modules.OcpiCredentialsModule;
import com.llocer.ev.ocpi.msgs22.OcpiClientInfo;
import com.llocer.ev.ocpi.msgs22.OcpiCredentials;
import com.llocer.ev.ocpi.msgs22.OcpiCredentialsRole;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoint;
import com.llocer.ev.ocpi.msgs22.OcpiEndpoints;
import com.llocer.ev.ocpi.msgs22.OcpiVersions;
import com.llocer.ev.ocpi.server.OcpiAgentId;
import com.llocer.ev.ocpi.server.OcpiConfig;
import com.llocer.ev.ocpi.server.OcpiLink;
import com.llocer.ev.ocpi.server.OcpiRequestData;
import com.llocer.ev.ocpi.server.OcpiResult;
import com.llocer.ev.ocpi.server.OcpiResult.OcpiResultEnum;
import com.llocer.ev.ocpi.server.OcpiServlet;

public class OcpiExample extends OcpiServlet {

	private static final long serialVersionUID = -4764547316713059395L;
	
	// in order to skip URI.resolve problems, do not use initial slash and end by slash:
	private static final String servletPath = "example/ocpi/"; 
	private static final String servletPath221 = servletPath+"221/";

	private static final SimpleMap< String /*own token*/, OcpiLink > linksByToken = 
			MemorySimpleMapFactory.make( String.class, OcpiLink.class);

	private static final OcpiCredentialsModule ocpiCredentialsModule = new OcpiCredentialsModule(linksByToken); 

	/*
	 * version and credentials
	 */

	@Override
	protected OcpiVersions[] getVersions() {
		OcpiVersions answer = new OcpiVersions();
		answer.setVersion( OcpiEndpoints.Version._2_2_1 );
		answer.setUrl( OcpiConfig.getPublicURI().resolve( servletPath221 ) );
		return new OcpiVersions[] { answer };
	}

	@Override
	protected OcpiEndpoints getEndpoints( String version ) {
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
		endpoint.setIdentifier( OcpiEndpoint.Identifier.TARIFFS );
		endpoint.setRole( OcpiEndpoint.InterfaceRole.SENDER );
		endpoints.getEndpoints().add( endpoint );

		endpoint = new OcpiEndpoint();
		endpoint.setIdentifier( OcpiEndpoint.Identifier.TOKENS );
		endpoint.setRole( OcpiEndpoint.InterfaceRole.RECEIVER );
		endpoints.getEndpoints().add( endpoint );

		for( OcpiEndpoint e : endpoints.getEndpoints() ) {
			e.setUrl( OcpiConfig.getPublicURI()
					.resolve( servletPath221+e.getIdentifier().toString()+"/" ));
		}
		
		return endpoints;
	}

	/*
	 * agents & execution
	 */
	
	@Override
	protected OcpiResult<?> execute( OcpiRequestData oreq ) throws Exception {
		return OcpiResultEnum.NOT_SUPPORTED_ENDPOINT;
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

	static {
		OcpiLink link = new OcpiLink();
		
		link.ownId = new OcpiAgentId( "ES", "TST" );
		link.ownCredentials = new OcpiCredentials();
		link.ownCredentials.setUrl( OcpiConfig.getPublicURI().resolve( "/example/ocpi/" ) );
		
		OcpiCredentialsRole cpoRole = new OcpiCredentialsRole();
		cpoRole.setRole( OcpiClientInfo.Role.OTHER ); 
		cpoRole.setCountryCode( link.ownId.countryCode );
		cpoRole.setPartyId( link.ownId.partyId );		
		link.ownCredentials.setRoles( Collections.singletonList( cpoRole ) );
		
		link.ownCredentials.setToken( "TEST" );
		
		ocpiCredentialsModule.allowLink( link );
	}
}
