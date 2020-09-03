/*
 * Licensed to Cisco Systems, Inc
 */
package com.mobileprivate.network.enterpriseOnboard;

import com.cisco.as.nso.exception.NSOException;
import com.tailf.conf.ConfXMLParam;

/**
 * Abstract actions API for NSO north bound return
 * 
 *  @author name:Krishnaswamy Venkatraman
 *  @author email:mukundnit@gmail.com
 */
public class ActionFactory {
	private ActionFactory() {}
	
	static Action getAction(String actionPoint
			               ,ConfXMLParam[] paramList) 
			throws NSOException {
		Action result = null;
		switch (actionPoint) {
			
		case "customer-vrf-action-point":
			result = new CustomerVrfActions(paramList);
			break;	
		default:
			throw new NSOException(
					EnterpriseOnboardErrorCodes.unknownServiceType
				   ,"Action " + actionPoint + " not implemented/linked.");
		}
		return result;
	}


}

