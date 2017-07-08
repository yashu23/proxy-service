package com.lucky5.proxy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lucky5.proxy.cache.EndpointCache;
import com.lucky5.proxy.impl.RestEndpointImpl;
import com.lucky5.proxy.impl.SoapEndpointImpl;

@RestController
public class ConfigurationViewController {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationViewController.class);
	
	@RequestMapping("/config")
	@ResponseBody
	public List<Map<String,String>> getEndpointConfiguration() {
		List<Map<String,String>> result = new ArrayList<Map<String, String>>();
		
		EndpointCache.getInstance().getAllEndpoints().forEach((k,v) -> {
			Map<String,String> properties = new HashMap<String, String>();
			properties.putAll(v.getProperties());
			properties.put("id",k);
			properties.put("desc",v.getDesc());
			result.add(properties);
		});
		return result;
	}
	
	
	@RequestMapping(value="/add",method=RequestMethod.POST,consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void addEndpoint(@RequestBody Map<String,String> requestProperties) {
		logger.debug("map from request " + requestProperties);
		
		String type =  requestProperties.get("type");
		if("soap".equalsIgnoreCase(type)) {
			SoapEndpointImpl endpoint = new SoapEndpointImpl();
			endpoint.setProperties(requestProperties);
			endpoint.setDesc(requestProperties.get("desc"));
			endpoint.setId(requestProperties.get("id"));
			EndpointCache.getInstance().addEndpoint(endpoint);
		}
		if("rest".equalsIgnoreCase(type)) {
			RestEndpointImpl endpoint = new RestEndpointImpl();
			endpoint.setProperties(requestProperties);
			endpoint.setDesc(requestProperties.get("desc"));
			endpoint.setId(requestProperties.get("id"));
			EndpointCache.getInstance().addEndpoint(endpoint);
		}
	}
}
