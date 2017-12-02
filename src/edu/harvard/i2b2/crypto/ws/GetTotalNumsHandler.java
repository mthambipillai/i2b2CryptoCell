package edu.harvard.i2b2.crypto.ws;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.harvard.i2b2.common.util.xml.XMLUtil;

public class GetTotalNumsHandler {
	private static Log log = LogFactory.getLog(CryptoService.class);
	
	public GetTotalNumsHandler(Log l){
		log = l;
	}
	
	public OMElement execute(OMElement getTotalNumsElement) throws Exception {
		Iterator<?> topelems = getTotalNumsElement.getChildElements();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element totalNums = doc.createElement("totalnums");
		Element body = doc.createElement("message_body");
		body.appendChild(totalNums);
		String status = "<response_header><result_status><status type=\"DONE\">processing completed</status></result_status></response_header>";
		String toRemove = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String clientPublicKey=null;
		String conceptPath=null;
		String header = null;
		String fromTime =null;
		String toTime=null;
		String distribution=null;
		while(topelems.hasNext()){
			OMElement om = (OMElement) topelems.next();
			if(om.getQName().toString().equals("message_body")){
				Iterator<?> bodyIt = om.getFirstElement().getChildElements();
				while(bodyIt.hasNext()){
					OMElement om2 = (OMElement) bodyIt.next();
					if(om2.getQName().toString().equals("pubkey")){
						clientPublicKey = om2.getText();
					}else if(om2.getQName().toString().equals("concept")){
						conceptPath = om2.getText();
					}else if(om2.getQName().toString().equals("fromtime")){
						fromTime = om2.getText();
					}else if(om2.getQName().toString().equals("totime")){
						toTime = om2.getText();
					}else if(om2.getQName().toString().equals("distribution")){
						distribution = om2.getText();
					}
				}
			}else if(om.getQName().toString().equals("message_header")){
				header =  om.toString();
			}
		}
		OMElement res=null;
		URL totalNumUrl;
		//MAKE JSON
		JSONObject o = new JSONObject();
		if(conceptPath!=null){
			o.put("conceptpath",conceptPath);
		}
		if(clientPublicKey!=null){
			o.put("clientpublickey",clientPublicKey);
		}
		if(fromTime!=null){
			o.put("fromtime",fromTime);
		}
		if(toTime!=null){
			o.put("totime",toTime);
		}
		if(distribution!=null){
			o.put("distribution",distribution);
		}
		
		//SEND REQUEST WITH JSON TO CRYPTO ENGINE
		log.info("GONNA SEND");
		totalNumUrl = new URL("http://127.0.0.1:7500/totalnums");
		HttpURLConnection conn = (HttpURLConnection) totalNumUrl.openConnection();
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		os.write(o.toString().getBytes("UTF-8"));
		os.close();
		
		//READ AND PARSE RESPONSE FROM CRYPTO ENGINE TO JSON
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    StringBuilder responseStrBuilder = new StringBuilder();
	    String inputStr;
	    while ((inputStr = rd.readLine()) != null){
	    	responseStrBuilder.append(inputStr);
	    }
	    
	    JSONObject totalnums = new JSONObject(responseStrBuilder.toString());
	    
	    //TRANSFORM JSON TO XML
	    JSONArray groups=null;
	    try{
	    	groups = totalnums.getJSONArray("groups");
	    }catch(Exception exception){
	    	log.info("EMPTY RESULT FROM CRYPTO ENGINE");
	    	String bodyStr = XMLUtil.convertDOMElementToString(body);
		    bodyStr = bodyStr.substring(toRemove.length(),bodyStr.length());
		    String xmlStr = "<response>"+header+status+bodyStr+"</response>";
		    res =AXIOMUtil.stringToOM(xmlStr);
			return res;
	    }
	    for(int i=0;i<groups.length();i++){
	    	JSONObject obj = groups.getJSONObject(i);
	    	Element totalNumGroup = doc.createElement("totalnum_group");
	    	
	    	String locationStr=null;
	    	Element time=null;
	    	if(distribution.equals("point")){
	    		String[] groupStr = obj.getString("group").split(",");
		    	locationStr = groupStr[0];
		    	String timeStr = groupStr[1];
		    	time = doc.createElement("time");
		    	time.appendChild(doc.createTextNode(timeStr));
	    	}else{
	    		locationStr = obj.getString("group");
	    	}
	    	
	    	Element location = doc.createElement("location");
	    	location.appendChild(doc.createTextNode(locationStr));
	    	
	    	Element totalNum = doc.createElement("totalnum");
	    	totalNum.appendChild(doc.createTextNode(obj.getString("totalnum")));
	    	
	    	totalNumGroup.appendChild(location);
	    	if(distribution.equals("point")){
	    		totalNumGroup.appendChild(time);
	    	}
	    	totalNumGroup.appendChild(totalNum);
	    	
	    	//log.info("XML : "+totalNumGroup.getTextContent());
	    	
	    	totalNums.appendChild(totalNumGroup);
	    	log.info("GROUP : "+obj.getString("group")+" "+obj.getString("totalnum"));
	    }
	    //log.info("DOC : "+XMLUtil.convertDOMToString(doc));
	    String bodyStr = XMLUtil.convertDOMElementToString(body);
	    bodyStr = bodyStr.substring(toRemove.length(),bodyStr.length());
	    String xmlStr = "<response>"+header+status+bodyStr+"</response>";
	    res =AXIOMUtil.stringToOM(xmlStr);
		return res;
	}
}
