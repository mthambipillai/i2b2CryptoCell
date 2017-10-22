/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crypto.util;
 
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.common.util.ServiceLocatorException;

public class QueryProcessorUtil {

	/** log **/
	protected final static Log log = LogFactory
			.getLog(QueryProcessorUtil.class);

	/** property file name which holds application directory name **/
	public static final String APPLICATION_DIRECTORY_PROPERTIES_FILENAME = "crypto_application_directory.properties";

	/** application directory property name **/
	public static final String APPLICATIONDIR_PROPERTIES = "edu.harvard.i2b2.crypto.applicationdir";

	/** application property filename* */
	public static final String APPLICATION_PROPERTIES_FILENAME = "crypto.properties";


	/** property name for ontology url schema name **/
	private static final String ONTOLOGYCELL_WS_URL_PROPERTIES = "queryprocessor.ws.ontology.url";

	public static final String ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES = "edu.harvard.i2b2.crypto.delegate.ontology.url";

	public static final String ONTOLOGYCELL_GETMODIFIERS_URL_PROPERTIES = "edu.harvard.i2b2.crypto.delegate.ontology.operation.getmodifiers";

	public static final String ONTOLOGYCELL_GETCHILDREN_URL_PROPERTIES = "edu.harvard.i2b2.crypto.delegate.ontology.operation.getchildren";

	public static final String ONTOLOGYCELL_GETMODIFIERCHILDREN_URL_PROPERTIES = "edu.harvard.i2b2.crypto.delegate.ontology.operation.getmodifierchildren";


	/** class instance field* */
	private static volatile QueryProcessorUtil thisInstance = null;

	/** service locator field* */
	private static ServiceLocator serviceLocator = null;

	/** field to store application properties * */
	private static Properties appProperties = null;

	private static Properties loadProperties = null;
		
	private static final Object lock = new Object();
	
	/**
	 * Private constructor to make the class singleton
	 */
	private QueryProcessorUtil() {


	}

	static {
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			log.error(e);

		}
	}

	/**
	 * Return this class instance
	 * 
	 * @return QueryProcessorUtil
	 */
	public static QueryProcessorUtil getInstance() {
		
		QueryProcessorUtil i = thisInstance;
		if (i == null) {
			synchronized (lock){
				i = thisInstance;
				if (i==null){
					i = new QueryProcessorUtil();
					thisInstance = i;
					serviceLocator = ServiceLocator.getInstance();

				}
			}

		}

		return i;
	}

	public String getOntologyUrl() throws I2B2Exception {
		return getPropertyValue(ONTOLOGYCELL_WS_URL_PROPERTIES);
	}
	public String getModifiersUrl() throws I2B2Exception {
		return getPropertyValue(ONTOLOGYCELL_WS_URL_PROPERTIES)+getPropertyValue(ONTOLOGYCELL_GETMODIFIERS_URL_PROPERTIES);
	}
	public String getModifierChildrenUrl() throws I2B2Exception {
		return getPropertyValue(ONTOLOGYCELL_WS_URL_PROPERTIES)+getPropertyValue(ONTOLOGYCELL_GETMODIFIERCHILDREN_URL_PROPERTIES);
	}
	public String getChildrenUrl() throws I2B2Exception {
		return getPropertyValue(ONTOLOGYCELL_WS_URL_PROPERTIES)+getPropertyValue(ONTOLOGYCELL_GETCHILDREN_URL_PROPERTIES);
	}

	/**
	 * Load application property file into memory
	 */
	private String getPropertyValue(String propertyName) throws I2B2Exception {
		log.info("BEGIN PROP");
		if (appProperties == null) {
			log.info("APPPROPNULL");
			// read application directory property file
			loadProperties = ServiceLocator
					.getProperties(APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			// read application directory property
			String appDir = loadProperties
					.getProperty(APPLICATIONDIR_PROPERTIES);
			if (appDir == null) {
				log.info("APPDIRNULL");
				throw new I2B2Exception("Could not find "
						+ APPLICATIONDIR_PROPERTIES + "from "
						+ APPLICATION_DIRECTORY_PROPERTIES_FILENAME);
			}
			String appPropertyFile = appDir + "/"
					+ APPLICATION_PROPERTIES_FILENAME;
			try {
				log.info("TRYING");
				FileSystemResource fileSystemResource = new FileSystemResource(
						appPropertyFile);
				PropertiesFactoryBean pfb = new PropertiesFactoryBean();
				pfb.setLocation(fileSystemResource);
				pfb.afterPropertiesSet();
				appProperties = (Properties) pfb.getObject();
			} catch (IOException e) {
				log.info("CAUGHT");
				throw new I2B2Exception("Application property file("
						+ appPropertyFile
						+ ") missing entries or not loaded properly");
			}
			if (appProperties == null) {
				log.info("APPPROPNULL2");
				throw new I2B2Exception("Application property file("
						+ appPropertyFile
						+ ") missing entries or not loaded properly");
			}
		}
		log.info("HERE PROP");
		String propertyValue = appProperties.getProperty(propertyName);

		if ((propertyValue != null) && (propertyValue.trim().length() > 0)) {
			;
		} else {
			throw new I2B2Exception("Application property file("
					+ APPLICATION_PROPERTIES_FILENAME + ") missing "
					+ propertyName + " entry");
		}

		return propertyValue;
	}

}
