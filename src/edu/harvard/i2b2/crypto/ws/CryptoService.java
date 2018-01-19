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


import java.util.Arrays;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.axis2.ServiceClient;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.crypto.util.QueryProcessorUtil;
import edu.harvard.i2b2.crypto.util.Roles;

public class CryptoService {
	private static Log log = LogFactory.getLog(CryptoService.class);
	public static final String i2b2CONNECTORADDRESS = "http://127.0.0.1:7500/";
	public static final String TOTALNUMENDPOINT = i2b2CONNECTORADDRESS+"totalnum";
	public static final String TOTALNUMSENDPOINT = i2b2CONNECTORADDRESS+"totalnums";
	public static final String ONTOLOGYURL = "http://localhost:9090/i2b2/services/OntologyService/";
	
	QueryProcessorUtil qpu = QueryProcessorUtil.getInstance();
	
	private GetChildrenHandler getChildrenHandler = new GetChildrenHandler(log);
	private GetTotalNumsHandler getTotalNumsHandler = new GetTotalNumsHandler(log);
	private AccessControl accessControl = new AccessControl(log);
	
	public OMElement getModifiers(OMElement getModifiersElement) throws I2B2Exception {
		log.info("GETMODIFIERS CALLED IN CRYPTO CELL");
		//String url = qpu.getModifiersUrl();
		String url = ONTOLOGYURL+"getModifiers";
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
		String ontologyUrl = ONTOLOGYURL+"getModifierChildren";
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
		//log.info("RECEIVED THIS FROM THE CLIENT :\n"+getTotalNumsElement);
		OMElement res = null;
		try{
			String[] roles = accessControl.getUserRoles(getTotalNumsElement);
			res = getTotalNumsHandler.execute(getTotalNumsElement, roles);
			//log.info("SENDING THIS TO THE CLIENT :\n"+res);
		}catch (Exception e){
			log.error(e.getMessage());
		}
		return res;
	}
	
	public OMElement getChildren(OMElement getChildrenElement) throws I2B2Exception {
		log.info("GETCHILDREN CALLED IN CRYPTO CELL");
		//log.info("RECEIVED THIS FROM THE CLIENT :\n"+getChildrenElement);
		OMElement res = null;
		try {
			String[] roles = accessControl.getUserRoles(getChildrenElement);
			if(AccessControl.checkRole(roles,Roles.totalNumsRoleName)){
				res = getChildrenHandler.execute(getChildrenElement,false,true);
				//log.info("SENDING THIS TO THE CLIENT :\n"+res);
			}else if(AccessControl.checkRole(roles,Roles.totalNumsNoisyRoleName)){
				res = getChildrenHandler.execute(getChildrenElement,true,true);
				//log.info("SENDING THIS TO THE CLIENT :\n"+res);
			}else{
				log.info("REQUIRE HIGHER ROLE PRIVILEGE TO HAVE TOTALNUMS\n");
				res = getChildrenHandler.execute(getChildrenElement,true,false);
				//log.info("SENDING THIS TO THE CLIENT :\n"+res);
			}
		} catch (Exception e){
			log.error(e.getMessage());
		}
		return res;
	}
}







