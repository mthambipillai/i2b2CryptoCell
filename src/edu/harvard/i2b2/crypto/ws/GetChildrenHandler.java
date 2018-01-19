package edu.harvard.i2b2.crypto.ws;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.harvard.i2b2.common.util.axis2.ServiceClient;
import edu.harvard.i2b2.common.util.xml.XMLUtil;

public class GetChildrenHandler {
	private static Log log = LogFactory.getLog(CryptoService.class);
	
	public GetChildrenHandler(Log l){
		log = l;
	}
	
	public OMElement execute(OMElement getChildrenElement, boolean noisy, boolean totalNumsRights) throws Exception {
		long start = System.currentTimeMillis();
		Iterator<?> topelems = getChildrenElement.getChildElements();
		String clientPublicKey=null;
		while(topelems.hasNext()){
			OMElement om = (OMElement) topelems.next();
			if(om.getQName().toString().equals("message_body")){
				Iterator<?> body = om.getFirstElement().getChildElements();
				while(body.hasNext()){
					OMElement om2 = (OMElement) body.next();
					if(om2.getQName().toString().equals("pubkey")){
						clientPublicKey = om2.getText();
						body.remove();
					}
				}
			}
		}
		//String ontologyUrl = QueryProcessorUtil.getInstance().getOntologyUrl();
		String ontologyUrl = CryptoService.ONTOLOGYURL+"getChildren";
		OMElement res=null;
		URL totalNumUrl;
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
		if(!totalNumsRights){
			return AXIOMUtil.stringToOM(response);
		}
		
		//MAKE JSON
		ArrayList<String> keys = findKeys(response);
		if(keys.isEmpty()){
			return AXIOMUtil.stringToOM(response);
		}
		JSONArray array = new JSONArray(keys);
		JSONObject o = new JSONObject();
		o.put("conceptpaths",array);
		if(clientPublicKey!=null){
			o.put("clientpublickey",clientPublicKey);
		}
		o.put("noisy",noisy);
		
		//SEND REQUEST WITH JSON TO CRYPTO ENGINE
		totalNumUrl = new URL(CryptoService.TOTALNUMENDPOINT);
		HttpURLConnection conn=null;
		try{
			conn = (HttpURLConnection) totalNumUrl.openConnection();
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(o.toString().getBytes("UTF-8"));
			os.close();
		}catch(Exception connE){
			log.info(connE);
			return AXIOMUtil.stringToOM(response);
		}
		
		long end = System.currentTimeMillis();
		long computationTime = end -start;
		Date date = new Date(computationTime);
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		log.info("COMPUTATION TIME BEFORE CRYPTO ENGINE : "+formatter.format(date));
		
		//READ AND PARSE RESPONSE FROM CRYPTO ENGINE
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    StringBuilder responseStrBuilder = new StringBuilder();
	    String inputStr;
	    while ((inputStr = rd.readLine()) != null){
	    	responseStrBuilder.append(inputStr);
	    }
	    start = System.currentTimeMillis();
	    
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
		res =AXIOMUtil.stringToOM(modified);
		
		end = System.currentTimeMillis();
		computationTime = end -start;
		date = new Date(computationTime);
		formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		log.info("COMPUTATION TIME AFTER CRYPTO ENGINE : "+formatter.format(date));
		
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
