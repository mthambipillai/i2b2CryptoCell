package edu.harvard.i2b2.crypto.ws;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.ontology.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.pm.ws.PMResponseMessage;
import edu.harvard.i2b2.pm.ws.PMServiceDriver;

public class AccessControl {
private static Log log = LogFactory.getLog(CryptoService.class);
	
	public AccessControl(Log l){
		log = l;
	}
	
	public String[] getUserRoles(OMElement request) throws Exception{
		String requestElementString = request.toString();
		CryptoDataMessage requestMsg = new CryptoDataMessage(requestElementString);
		MessageHeaderType header = requestMsg.getMessageHeaderType();	
		
		GetUserConfigurationType userConfigType = new GetUserConfigurationType();

		PMResponseMessage msg = new PMResponseMessage();
		StatusType procStatus = null;	
		String response = PMServiceDriver.getRoles(userConfigType,header);
		procStatus = msg.processResult(response);
		if(procStatus.getType().equals("ERROR")){
			return null;
		}
		return extractRoles(response);
	}
	
	private String[] extractRoles(String xml){
		int start = xml.indexOf("<role>");
		int end = xml.lastIndexOf("</role>");
		String rolesStr = xml.substring(start,end+7);
		String[] temp = rolesStr.split("</role>");
		for(int i=0;i<temp.length;i++){
			int index = temp[i].indexOf("<role>");
			temp[i] = temp[i].substring(index+6,temp[i].length());
		}
		return temp;
	}
	
	public static boolean checkRole(String[] roles, String requiredRole){
		for(int i=0;i<roles.length;i++){
			if(roles[i].equals(requiredRole)){
				return true;
			}
		}
		return false;
	}
	
}












