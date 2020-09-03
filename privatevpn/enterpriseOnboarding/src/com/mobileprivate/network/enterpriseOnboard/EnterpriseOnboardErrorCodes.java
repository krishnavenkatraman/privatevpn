/*
 * Licensed to Cisco Systems, Inc
 */
package com.mobileprivate.network.enterpriseOnboard;

import com.cisco.as.nso.exception.ErrorCodes;

/**
 * EnterpriseOnboard specific Error Codes
 * Some error code may be incorrectly moved to common error code (parent)
 * file, need to be moved back.
 * 
 *  @author name:Krishnaswamy Venkatraman
 *  @author email:mukundnit@gmail.com
 */
public class EnterpriseOnboardErrorCodes extends ErrorCodes{
	// example
	public static final String dummy = "00000";


    static {
    	errorCodeMap.put(dummy, "a demo error code.");
    }
}
