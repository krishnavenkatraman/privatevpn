/*
 * Licensed to Cisco Systems, Inc
 */
package com.mobileprivate.network.enterpriseOnboard;

import java.io.IOException;

import com.mobileprivate.network.enterpriseOnboard.namespaces.enterpriseOnboard;
import com.tailf.conf.ConfObject;
import com.tailf.conf.ConfTag;
import com.tailf.conf.ConfXMLParam;
import com.tailf.dp.DpActionTrans;
import com.tailf.dp.DpCallbackException;
import com.tailf.dp.annotations.ActionCallback;
import com.tailf.dp.proto.ActionCBType;

/**
 * Customer Vrf service api
 * 
 *  @author name:Krishnaswamy Venkatraman
 *  @author email:mukundnit@gmail.com
 */
public class CustomerVrfApi extends AbstractApi {
    private static final String ACTION_POINT = enterpriseOnboard.actionpoint_customer_vrf_action_point;

    /**
     * No code beyond this point needs to be modified for a specific service
     */
    
    @Override
    @ActionCallback(callPoint = ACTION_POINT, callType = ActionCBType.INIT)
    public void init(DpActionTrans trans) throws DpCallbackException {
    }

    @Override
    @ActionCallback(callPoint = ACTION_POINT, callType = ActionCBType.ACTION)
    public ConfXMLParam[] action(DpActionTrans trans, 
    		                     ConfTag name,
                                 ConfObject[] kp, 
                                 ConfXMLParam[] params) 
    throws DpCallbackException, IOException {
        return routineAction(ACTION_POINT, trans, name, kp, params);
    }
}
