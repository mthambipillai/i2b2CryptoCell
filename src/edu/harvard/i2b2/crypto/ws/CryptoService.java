/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 *     Mike Mendis - initial API and implementation
 */

package edu.harvard.i2b2.crypto.ws;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.xml.XMLUtil;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import edu.harvard.i2b2.crypto.util.QueryProcessorUtil;

public class CryptoService {
	private static Log log = LogFactory.getLog(CryptoService.class);
	
	QueryProcessorUtil qpu = QueryProcessorUtil.getInstance();
	
	private GetChildrenHandler getChildrenHandler = new GetChildrenHandler(log);
	private GetTotalNumsHandler getTotalNumsHandler = new GetTotalNumsHandler(log);
	
	public OMElement getModifiers(OMElement getModifiersElement) throws I2B2Exception {
		log.info("GETMODIFIERS CALLED IN CRYPTO CELL");
		//String url = qpu.getModifiersUrl();
		String url = "http://localhost:9090/i2b2/services/OntologyService/getModifiers";
		OMElement res=null;
		try {		
			String response = ServiceClient.sendREST(url, getModifiersElement);
			res =AXIOMUtil.stringToOM(response);
		}catch (Exception e){
			log.error(e.getMessage());
		}
		return res;
	}
	
	public OMElement getModifierChildren(OMElement getModifierChildrenElement) throws I2B2Exception {
		log.info("GETMODIFIERCHILDREN CALLED IN CRYPTO CELL");
		//String ontologyUrl = QueryProcessorUtil.getInstance().getOntologyUrl();
		String ontologyUrl = "http://localhost:9090/i2b2/services/OntologyService/getModifierChildren";
		OMElement res=null;
		try {
			String response = ServiceClient.sendREST(ontologyUrl, getModifierChildrenElement);
			res =AXIOMUtil.stringToOM(response);
		}catch (Exception e){
			log.error(e.getMessage());
		}
		return res;
	}
	
	public OMElement getTotalNums(OMElement getTotalNumsElement) throws I2B2Exception {
		log.info("GETTOTALNUMS CALLED IN CRYPTO CELL");
		log.info("RECEIVED THIS FROM THE CLIENT :\n"+getTotalNumsElement);
		OMElement res = null;
		try{
			res = getTotalNumsHandler.execute(getTotalNumsElement);
			log.info("SENDING THIS TO THE CLIENT :\n"+res);
		}catch (Exception e){
			log.error(e.getMessage());
		}
		return res;
	}
	
	public OMElement getChildren(OMElement getChildrenElement) throws I2B2Exception {
		log.info("GETCHILDREN CALLED IN CRYPTO CELL");
		log.info("RECEIVED THIS FROM THE CLIENT :\n"+getChildrenElement);
		OMElement res = null;
		try {
			res = getChildrenHandler.execute(getChildrenElement);
			log.info("SENDING THIS TO THE CLIENT :\n"+res);
		} catch (Exception e){
			log.error(e.getMessage());
		}
		return res;
	}
}







