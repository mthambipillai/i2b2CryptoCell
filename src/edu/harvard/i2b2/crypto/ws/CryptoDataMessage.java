package edu.harvard.i2b2.crypto.ws;

import edu.harvard.i2b2.common.exception.I2B2Exception;

public class CryptoDataMessage extends RequestDataMessage{

	public CryptoDataMessage(String requestVdo) throws I2B2Exception {
		super(requestVdo);
	}

}
