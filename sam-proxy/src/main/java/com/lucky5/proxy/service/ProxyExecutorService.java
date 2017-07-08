package com.lucky5.proxy.service;

import org.springframework.stereotype.Service;

import com.lucky5.proxy.Endpoint;
import com.lucky5.proxy.cache.EndpointCache;


@Service
public class ProxyExecutorService {
	
	public String executeSOAP(String endpointId, String postBody) {
		Endpoint endPoint = EndpointCache.getInstance().getEndpoint(endpointId);
		return endPoint.getResponse(postBody);
	}

}
