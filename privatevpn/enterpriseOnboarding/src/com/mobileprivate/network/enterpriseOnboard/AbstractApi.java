/**
 * @copyright Cisco Systems, Inc 2016
 */
package com.mobileprivate.network.enterpriseOnboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cisco.as.nso.exception.NSOException;
import com.cisco.as.nso.utility.Utility;
import com.tailf.conf.ConfObject;
import com.tailf.conf.ConfTag;
import com.tailf.conf.ConfXMLParam;
import com.tailf.dp.DpActionTrans;
import com.tailf.dp.DpCallbackException;


/**
 * Abstract service api
 * 
 * @author name:Krishnaswamy Venkatraman
 * @author email:mukundnit@gmail.com
 * 
 */
public abstract class AbstractApi {
    abstract public void init(DpActionTrans trans) throws DpCallbackException;
    
    abstract public ConfXMLParam[] action(DpActionTrans trans, ConfTag name,
            ConfObject[] kp, ConfXMLParam[] params) throws DpCallbackException, IOException;

    public ConfXMLParam[] routineAction(String actionPoint, DpActionTrans trans, ConfTag name,
            ConfObject[] kp, ConfXMLParam[] params) throws DpCallbackException, IOException {
        ConfXMLParam[] result = null;

        try {
            Action action = ActionFactory.getAction(actionPoint, params);
            trans.actionSetTimeout(Utility.socketTimeoutInterval);

            action.serviceEventHandler(params);

            // Create the return array
            result = action.output.toArray(new ConfXMLParam[0]);
        } catch (NSOException e) {
            List<ConfXMLParam> output = new ArrayList<ConfXMLParam>();
            Utility.addResponseHeaders(output, params);
            Utility.setErrorReturn(
                    output,
                    Integer.parseInt(e.getErrorCode().replaceAll(
                            "[^\\d]", "")), 
                    e.getErrorText());
            result = output.toArray(new ConfXMLParam[0]);
        }
        return (result);
    }
}
