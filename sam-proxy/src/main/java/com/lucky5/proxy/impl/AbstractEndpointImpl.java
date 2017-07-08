package com.lucky5.proxy.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.lucky5.proxy.Endpoint;

public abstract class AbstractEndpointImpl implements Endpoint {	
	private String id;
	private String desc;
	private CloseableHttpClient httpClient;
	private PoolingHttpClientConnectionManager cpm;
	private Map<String,String> properties = new HashMap<String,String>();
	private static final Logger logger = LoggerFactory.getLogger(AbstractEndpointImpl.class);
	private boolean stopFlag = false;

	public AbstractEndpointImpl() {
		super();
	}	

	public AbstractEndpointImpl(final String id,final String desc,final Map<String,String> properties) {
		super();
		this.id = id;
		this.desc = desc;
		this.properties = properties;
	}
	
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(final String desc) {
		this.desc = desc;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public String getPropertyValue(final String key) {
		return getPropertyValue(key, null);
	}
	
	public String getPropertyValue(final String key, final String defaultValue) {
		if (properties != null) {
			String valueFromMap = properties.get(key);
			if (StringUtils.isEmpty(valueFromMap)) {
				return StringUtils.isEmpty(defaultValue) ? "" : defaultValue;
			} else
				return valueFromMap;
		}
		throw new IllegalStateException("null properties object");
	}

	public int getPropertyValueAsInt(final String key, final int defaultValue) {
		if (properties != null) {
			try {
				int valueFromMap = Integer.parseInt(properties.get(key));
				return valueFromMap;
			} catch (NumberFormatException ex) {
				logger.warn("invalid integer value " + properties.get(key));
			}
		}
		return defaultValue;
	}
	
	
	/*
	 * Set new connection pool
	 * 
	 */
	public synchronized void createConnectionPool() {		
		cpm = new PoolingHttpClientConnectionManager();
		logger.debug("Default Concurrent connections : " + cpm.getDefaultMaxPerRoute());

		cpm.setMaxTotal(getPropertyValueAsInt("max-connections",1));
		cpm.setDefaultMaxPerRoute(getPropertyValueAsInt("max-connections",1));
		logger.debug("Default Concurrent connections after changing: " + cpm.getDefaultMaxPerRoute());

		// Create socket configuration
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
		cpm.setDefaultSocketConfig(socketConfig);
		cpm.setSocketConfig(new HttpHost(getPropertyValue("url")), socketConfig);
		cpm.closeIdleConnections(30, TimeUnit.SECONDS);

		// Create an ClosableHttpClient with the ThreadSafeClientConnManager.
		// This connection manager must be used if more than one thread will
		// be using the HttpClient.

		HttpClientBuilder builder = HttpClientBuilder.create();
		RequestConfig rc = RequestConfig.custom()
				.setSocketTimeout(getPropertyValueAsInt("requestTimeout",30000))
				.setConnectTimeout(getPropertyValueAsInt("connectionTimeout",30000)).build();
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		builder.setConnectionManager(cpm);
		builder.setDefaultRequestConfig(rc);
		builder.setDefaultCredentialsProvider(credentialsProvider);
		httpClient = builder.build();
	}
	
	protected void renewConnection() {
		try {
			if(httpClient != null)
				httpClient.close();			
		}
		catch(Exception ex){
			logger.error("error in releasing connection " + ex);
		}
	}
	
	public void releaseResource(InputStream in) {
		if(in != null) {
			try {
				in.close();
			} catch (IOException e) {
				logger.error("unable to close iostream " + e.getLocalizedMessage());
			}
		}
	}
	
	public void releaseRequest(HttpPost httpRequest) {
		try {
			httpRequest.abort();
			httpRequest.releaseConnection();
		}
		catch(Exception ex){
			logger.error("error in releasing connection " + ex);
		}
	}

	public synchronized CloseableHttpClient getHttpClient() {
		if(httpClient == null) {
			createConnectionPool();
		}
		return httpClient;
	}

	public PoolingHttpClientConnectionManager getPoolManager() {
		return cpm;
	}
	
	@Override	
	public void stopAcceptingRequest() {
		stopFlag = true;
	}
	
	@Override
	public boolean getStopFlag() {
		return stopFlag;
	}
}
