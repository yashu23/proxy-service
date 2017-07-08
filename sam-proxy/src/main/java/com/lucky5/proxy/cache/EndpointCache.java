package com.lucky5.proxy.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.lucky5.proxy.Endpoint;

/*
 * All connections will be requested via connection factory 
 * 
 */
public class EndpointCache {	
	private static EndpointCache instance;
	private final Map<String,Endpoint> endpoints = new HashMap<String,Endpoint>();
	private static final Logger logger = LoggerFactory.getLogger(EndpointCache.class);
	
	private EndpointCache() {}
	
	public static final EndpointCache getInstance(){
		if(instance == null) {
			instance = new EndpointCache();
			new Thread(new WatcherThread(instance)).start();
		}
		return instance;
	}
	
	/*
	 * reset cache to pick up modified endpoints
	 * 
	 * This method should reset cache 
	 */
	public synchronized void resetCache(List<Endpoint> updatedEndpoints) {
		for(Endpoint endpoint : updatedEndpoints) {
			if(!StringUtils.isEmpty(endpoints.get(endpoint.getId()))){
				Endpoint endpointRef = endpoints.get(endpoint.getId());
				
				endpointRef.stopAcceptingRequest();
				
				new Thread(new Runnable(){
					@Override
					public void run() {
						if(endpointRef.getPoolManager() != null) {										
							while(true) {							
								int pending = endpointRef.getPoolManager().getTotalStats().getPending();
								int leased = endpointRef.getPoolManager().getTotalStats().getLeased();
								
								if(pending == 0 && leased == 0) {
									logger.info("no active connections for this endpoint, hence destroying it");
									getAllEndpoints().put(endpoint.getId(),endpointRef);									
								}
								try {
									TimeUnit.SECONDS.sleep(1);
								}
								catch(InterruptedException ex){
									logger.error("exception occurred",ex);
								}
							}
						}
					}					
				}).start();
			}
			else {
				endpoints.put(endpoint.getId(),endpoint);
			}
		}
	}
	
	public Endpoint getEndpoint(final String id) {
		Endpoint endpoint = endpoints.get(id);
		if(endpoint == null || endpoint.getStopFlag()) {
			logger.error("requested endpoint " + id + " does not exists");
			throw new IllegalStateException("No Endpoint configured for the service " + id);
		}
		return endpoint;
	}
	
	public synchronized void addEndpoint(Endpoint endpoint) {
		endpoints.put(endpoint.getId(),endpoint);
	}
	
	public synchronized void stopEndpoint(Endpoint endpoint) {
		endpoint.stopAcceptingRequest();
	}
	
	public Map<String,Endpoint> getAllEndpoints() {
		return endpoints;
	}
	
	/*
	 * Method to print pool statistics 
	 */
	public void printPoolStats() {
		logger.debug("printing pool stats " + getAllEndpoints());
		getAllEndpoints().forEach((k,v) -> {
			
			if(v.getPoolManager() != null) {
				int pending = v.getPoolManager().getTotalStats().getPending();
				int leased = v.getPoolManager().getTotalStats().getLeased();
				int available = v.getPoolManager().getTotalStats().getAvailable();
				logger.info(k + ": Pending = " + pending + ", Leased = " + leased + ", Available = " + available);
			}
			else
				logger.warn("no pool manager present");
		});
	}
	
	/*
	 * Remove endpoints which has already been stopped	
	 */
	public void removeStoppedEndpoint(){
		logger.debug("remove stopped endpoint");
		getAllEndpoints().forEach((k,v) -> {
			
			if(v.getPoolManager() != null) {
				int pending = v.getPoolManager().getTotalStats().getPending();
				int leased = v.getPoolManager().getTotalStats().getLeased();
							
				if(v.getStopFlag()) {
					logger.warn("this endpoint is marked for destroy");
					if(pending == 0 && leased == 0) {
						logger.info("no active connections for this endpoint, hence destroying it");						
						getAllEndpoints().remove(k);
					}
				}
			}
			else
				logger.warn("no pool manager present");
		});
	}
}

/*
 * Watcher thread
 * 
 */
class WatcherThread implements Runnable {
	private EndpointCache cache;
	private static final Logger logger = LoggerFactory.getLogger(WatcherThread.class);
		
	public WatcherThread(EndpointCache cache) {
		this.cache = cache;
	}
	
	@Override
	public void run() {
		logger.debug("starting watcher thread");
		while(true) {
			try {
				cache.removeStoppedEndpoint();
				cache.printPoolStats();
				logger.debug("sleeping thread for 30 secs");
				TimeUnit.SECONDS.sleep(30);
			}
			catch(Exception ex){
				logger.error("error occurred " + ex);
			}
		}
	}	
}