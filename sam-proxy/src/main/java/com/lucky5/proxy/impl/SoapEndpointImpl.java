package com.lucky5.proxy.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapEndpointImpl extends AbstractEndpointImpl{
	
	private static final Logger logger = LoggerFactory.getLogger(SoapEndpointImpl.class);

	@Override
	public String getResponse(String request) {
		InputStream instream = null;
		HttpResponse httpResponse = null;
		HttpPost httpRequest = null;
		try {
			httpRequest = new HttpPost(getPropertyValue("url"));
			StringEntity se = new StringEntity(request);
			httpRequest.setHeader("Content-Type","text/xml");
			httpRequest.setEntity(se);
			httpResponse = getHttpClient().execute(httpRequest);
	
			HttpEntity entity = httpResponse.getEntity();
	
			if (entity != null) {	
				instream = entity.getContent();
	
				//logger.debug(" InputStreamReader start: "+ (new Timestamp(System.currentTimeMillis())).toString());
				String inputLine;
				StringBuffer responseSB = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(instream));
				while ((inputLine = br.readLine()) != null) {
					//logger.debug(inputLine);
					responseSB.append(inputLine);
				}
				//logger.debug(" InputStreamReader end: "+ (new Timestamp(System.currentTimeMillis())).toString());
				String response = responseSB.toString();
				br.close();	
								
				return response;
			}	
		}		
		catch(Exception ex){
			logger.error("exception occured on connection due to " + ex.getLocalizedMessage());
			logger.warn("aborting current request and releasing connection back to pool");
			releaseRequest(httpRequest);
			releaseResource(instream);
		}
		return null;
	}
	
	

	
}
