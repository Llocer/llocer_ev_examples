package com.llocer.ev.example;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.llocer.common.Log;
import com.llocer.ev.ocpp.server.OcppEndpoint;

@WebListener
public class MainStartStop implements ServletContextListener {

	@Override
	public void contextInitialized( ServletContextEvent sce ) {
		/*
		 * register OcppServerExample agent
		 */
		
		try {
			OcppServerExample it = new OcppServerExample();
			OcppEndpoint.putAgent( it );
		} catch (Exception e) {
			Log.error( e, "error at initilization");
		}
		
		/*
		 * send a location
		 */
		
	    TimerTask task = new TimerTask() {
	        public void run() {
	        	try {
	        		OcpiTestLocationReceiver.makeLink();
					OcpiTestLocationSender.test1();
				} catch (Exception e) {
					Log.debug( e, "Test exception" );
				}
	        }
	    };
	    Timer timer = new Timer("Timer");
	    
	    long delay = 10000L;
	    timer.schedule(task, delay);
	}

	@Override
	public void contextDestroyed( ServletContextEvent sce ) {
	}
	
}
