package com.lucky5.proxy;

import java.util.Map;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public interface Endpoint {	
	public String getId();
	public String getDesc();
	public String getResponse(String request);
	public void stopAcceptingRequest();
	public boolean getStopFlag();
	public Map<String,String> getProperties();
	public PoolingHttpClientConnectionManager getPoolManager();
	public void setProperties(final Map<String,String> properties);
}
