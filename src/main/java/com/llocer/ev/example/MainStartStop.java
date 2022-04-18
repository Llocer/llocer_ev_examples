package com.llocer.ev.example;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.llocer.common.Log;
import com.llocer.ev.ocpp.server.OcppEndpoint;

@WebListener
public class MainStartStop implements ServletContextListener {

	@Override
	public void contextInitialized( ServletContextEvent sce ) {
		try {
			OcppExample it = new OcppExample();
			OcppEndpoint.putAgent( it );
		} catch (Exception e) {
			Log.error( e, "error at initilization");
		}
	}

	@Override
	public void contextDestroyed( ServletContextEvent sce ) {
	}
	
}
