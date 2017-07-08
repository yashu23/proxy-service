package com.lucky5.proxy.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lucky5.proxy.service.ProxyExecutorService;

@Controller
public class ProxyController {
	
	@Autowired
	private ProxyExecutorService service;
	
	@RequestMapping(method=RequestMethod.POST,value={"/soap/{endpointId}"},produces=MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public String getSoapResponse(@PathVariable String endpointId,HttpServletRequest request) {
		/*
		 * getConnection
		 * service.executeSOAP(endpointId,requestXml)
		 * 
		 * 
		 */
		return service.executeSOAP(endpointId,getPostBody(request));
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value={"/rest/{endpointId}"},produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getRestResponse(@PathVariable String endpointId) {
		/*
		 * getConnection
		 * service.executeRest(endpointId)
		 * 
		 * 
		 */
		return "{\"string\": \"value\"}";
	}
	
	/*
	 * Read request xml in the servlet request
	 * 
	 */
	public String getPostBody(HttpServletRequest request) {
		try {
		if(request != null) {
			StringBuilder strb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String temp = null;
			
			while((temp = br.readLine()) != null) {
				strb.append(temp);
			}			
			return strb.toString();
		}
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
