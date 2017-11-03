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
		Iterator topelems = getTotalNumsElement.getChildElements();
		String clientPublicKey=null;
		String conceptPath=null;
		while(topelems.hasNext()){
			OMElement om = (OMElement) topelems.next();
			if(om.getQName().toString().equals("message_body")){
				Iterator body = om.getFirstElement().getChildElements();
				while(body.hasNext()){
					OMElement om2 = (OMElement) body.next();
					if(om2.getQName().toString().equals("pubkey")){
						clientPublicKey = om2.getText();
					}else if(om2.getQName().toString().equals("concept")){
						conceptPath = om2.getText();
					}
				}
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
	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element totalNums = doc.createElement("totalnums");
	    JSONArray groups=null;
	    try{
	    	groups = totalnums.getJSONArray("groups");
	    }catch(Exception exception){
	    	throw new Exception("EMPTY RESULT FROM CRYPTO ENGINE");
	    }
	    for(int i=0;i<groups.length();i++){
	    	JSONObject obj = groups.getJSONObject(i);
	    	Element totalNumGroup = doc.createElement("totalnum_group");
	    	
	    	String[] groupStr = obj.getString("group").split(",");
	    	String locationStr = groupStr[0];
	    	String timeStr = groupStr[1];
	    	
	    	Element location = doc.createElement("location");
	    	location.appendChild(doc.createTextNode(locationStr));
	    	
	    	Element time = doc.createElement("time");
	    	time.appendChild(doc.createTextNode(timeStr));
	    	
	    	Element totalNum = doc.createElement("totalnum");
	    	totalNum.appendChild(doc.createTextNode(obj.getString("totalnum")));
	    	
	    	totalNumGroup.appendChild(location);
	    	totalNumGroup.appendChild(time);
	    	totalNumGroup.appendChild(totalNum);
	    	
	    	//log.info("XML : "+totalNumGroup.getTextContent());
	    	
	    	totalNums.appendChild(totalNumGroup);
	    	log.info("GROUP : "+obj.getString("group")+" "+obj.getString("totalnum"));
	    }
	    //log.info("DOC : "+XMLUtil.convertDOMToString(doc));
	    res =AXIOMUtil.stringToOM(XMLUtil.convertDOMElementToString(totalNums));
		return res;
	}
}
