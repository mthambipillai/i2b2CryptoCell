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
	
	public OMElement getModifiers(OMElement getModifiersElement) throws I2B2Exception {
		log.info("GETMODIFIERS CALLED IN CRYPTO CELL");
		//String url = qpu.getModifiersUrl();
		String url = "http://localhost:9090/i2b2/services/OntologyService/getModifiers";
		OMElement res=null;
		try {		
			String response = ServiceClient.sendREST(url, getModifiersElement);
			res =AXIOMUtil.stringToOM(response);
		} catch (Exception e){
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
		} catch (Exception e)
		{
			log.error(e.getMessage());
		}
		
		return res;
	}
	
	public OMElement getChildren(OMElement getChildrenElement) throws I2B2Exception {
		log.info("GETCHILDREN CALLED IN CRYPTO CELL");
		//String ontologyUrl = QueryProcessorUtil.getInstance().getOntologyUrl();
		String ontologyUrl = "http://localhost:9090/i2b2/services/OntologyService/getChildren";
		OMElement res=null;
		URL totalNumUrl;
		try {
			//GET RESPONSE FROM ONTOLOGY CELL AND PARSE THE XML
			String response = ServiceClient.sendREST(ontologyUrl, getChildrenElement);
			Document respXML = XMLUtil.convertStringToDOM(response);
			Node e = respXML.getDocumentElement();
			NodeList elements = e.getChildNodes();
			NodeList conceptsXML=null;
			for(int i=0;i<elements.getLength();i++){
				Node n = elements.item(i);
				if(n.getNodeName().equals("message_body")){
					conceptsXML = n.getChildNodes().item(1).getChildNodes();
				}
			}
			
			//MAKE JSON
			ArrayList<String> keys = findKeys(response);
			JSONArray array = new JSONArray(keys);
			JSONObject o = new JSONObject();
			o.put("conceptpaths",array);
			
			//SEND REQUEST WITH JSON TO CRYPTO ENGINE
			totalNumUrl = new URL("http://127.0.0.1:7500/totalnum");
			HttpURLConnection conn = (HttpURLConnection) totalNumUrl.openConnection();
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(o.toString().getBytes("UTF-8"));
			os.close();
			
			//READ AND PARSE RESPONSE FROM CRYPTO ENGINE
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    StringBuilder responseStrBuilder = new StringBuilder();
		    String inputStr;
		    while ((inputStr = rd.readLine()) != null){
		    	responseStrBuilder.append(inputStr);
		    }
		    
		    HashMap<String,String> pathsToNums = new HashMap<String,String>();
		    
		    JSONObject totalnums = new JSONObject(responseStrBuilder.toString());
		    JSONArray concepts=null;
		    try{
		    	concepts = totalnums.getJSONArray("concepts");
		    }catch(Exception exception){
		    	log.info("EMPTY RESULT FROM CRYPTO ENGINE");
		    	return AXIOMUtil.stringToOM(response);
		    }
		    for(int i=0;i<concepts.length();i++){
		    	JSONObject obj = concepts.getJSONObject(i);
		    	pathsToNums.put(obj.getString("conceptpath"),obj.getString("totalnum"));
		    }
		    
		    //REPLACE TOTALNUMS IN XML AND SEND BACK TO CLIENT
		    for(int i=0;i<conceptsXML.getLength();i++){
		    	setTotalNum(conceptsXML.item(i),pathsToNums);
		    }
		    String modified = XMLUtil.convertDOMToString(respXML);
			log.info("SENDING THIS TO THE CLIENT :\n"+modified);
			res =AXIOMUtil.stringToOM(modified);
		} catch (Exception e)
		{
			log.error(e.getMessage());
		}
		
		return res;
	}
	
	private ArrayList<String> findKeys(String xml){
		ArrayList<String> keys = new ArrayList<String>();
		int i = xml.indexOf("<key>");
		int j=xml.indexOf("</key>");
		while(i>=0 && j>=0){
			keys.add(xml.substring(i+5,j));
			i = xml.indexOf("<key>",i+1);
			j=xml.indexOf("</key>",j+1);
		}
		return keys;
	}
	
	private void setTotalNum(Node n, HashMap<String,String> pathsToNums){
		NodeList nl = n.getChildNodes();
		Node key=null;
		Node totalNumElem=null;
		for(int i=0;i<nl.getLength();i++){
			if(nl.item(i).getNodeName().equals("key")){
				key = nl.item(i);
			}else if(nl.item(i).getNodeName().equals("totalnum")){
				totalNumElem = nl.item(i);
			}
		}
		if(key==null || pathsToNums==null){
			return;
		}
		String totalNum = pathsToNums.get(key.getTextContent());
		if(totalNum!=null && totalNumElem!=null){
			totalNumElem.setTextContent(totalNum);
		}
	}
}







