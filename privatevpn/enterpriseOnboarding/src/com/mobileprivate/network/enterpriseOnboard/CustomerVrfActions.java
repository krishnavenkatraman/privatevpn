/*
 * @copyright Cisco Systems, Inc 2016
 */
package com.mobileprivate.network.enterpriseOnboard;

import java.io.IOException;
import java.util.ArrayList;
import com.tailf.conf.ConfList;
import com.cisco.as.nso.exception.NSOException;
import com.cisco.as.nso.utility.*;
import com.mobileprivate.network.enterpriseOnboard.namespaces.enterpriseOnboard;
import com.tailf.conf.ConfBuf;
import com.tailf.conf.ConfEnumeration;
import com.tailf.conf.ConfException;
import com.tailf.conf.ConfKey;
import com.tailf.conf.ConfUInt32;
import com.tailf.conf.ConfXMLParam;
import com.tailf.navu.NavuContainer;
import com.tailf.navu.NavuContext;
import com.tailf.navu.NavuException;
import com.tailf.navu.NavuList;
import com.tailf.navu.NavuNode;
import com.tailf.ncs.ns.Ncs;

/**
 * Customer Vrf actions
 * 
 * REVISION HISTORY:
 * By                        Date      ContactInfo      	Reason
 * ------------------------------------------------------------
 * Krishnaswamy Venkatraman	NA			mukundnit@gmail.com	Initial cr
 * 
 */
public class CustomerVrfActions extends Action {

    private String customerId;
    private String customerName;
    private String rdVal;
    private String rtExport;
    // private String rtImport;
    private int enum_noCpe; // Enum for no-cpe

    /**
     * Concrete Constructor
     */
    public CustomerVrfActions(ConfXMLParam[] paramList)
            throws NSOException {
    	super(paramList);

        LOGGER.info("Loading Input Parameter values in VRF Constructor");

        customerName = Utility.getStringParam(params,
                enterpriseOnboard._customer_name_);
        customerId = Utility.getStringParam(params,
                enterpriseOnboard._customer_id_);
        device = Utility.getStringParam(params,
                enterpriseOnboard._device_);
        requestId = Utility.getStringParam(params,
                enterpriseOnboard._request_id_);
        // removed null check because they were null anyway
        rdVal = Utility.getStringParam(params,
                    enterpriseOnboard._rd_);
        rtExport = Utility.getStringParam(params,
                    enterpriseOnboard._rt_export_);

        paramMap.put(enterpriseOnboard._rt_import_, Utility
                .getParamLeafList(params, enterpriseOnboard._rt_import_, " "));
  
        int noCpeFlag = Utility.getEnumStringParam(params,
                enterpriseOnboard._no_cpe_);
        if (noCpeFlag != -1) {
            enum_noCpe = noCpeFlag;
        } else {
            enum_noCpe = 1; // not given, so treat it as no-cpe = "no"
        }

        // Create ConfBuf Array with Composite Keys
        numOfKeyElements = 2;
        compositeKeyElems = new ConfBuf[numOfKeyElements];

        compositeKeyElems[0] = new ConfBuf(customerId);
        compositeKeyElems[1] = new ConfBuf(device);

        // Generate String Array with Key Elements from ConfBuf Array
        keyElementsArray = Utility
                .getStringArrayFromConfBufArray(compositeKeyElems);

        // Set action handler
        actionHandler = new Actions();
        actionHandler.setKeyElement1(customerId);
        actionHandler.setHashValueforKeyElement1(enterpriseOnboard._customer_id);
        
        // Set Service Name
        serviceName = enterpriseOnboard._customer_vrf_;
        Utility.DumpMessage("Dump Input Message: ",
                params, 
                serviceName);

        LOGGER.info("Finished Loading Input Parameter values in constructor");
    }
    
    @Override
	void specifyRequiredActionParameters(ConfXMLParam[] params) {
    	primaryArgs.put(enterpriseOnboard._customer_id_, false);

        // Set Secondary Parameters in Map
        secondaryArgs.put(enterpriseOnboard._customer_name_, false);
        secondaryArgs.put(enterpriseOnboard._rd_, false);
        secondaryArgs.put(enterpriseOnboard._rt_export_, false);
        secondaryArgs.put(enterpriseOnboard._rt_import_, false);
	}
    
    @Override
    protected final void saveRollback() throws NavuException {
    	actionHandler.saveRollbackForGivenService(params, requestId, customerId,
				serviceName + Utility.getRollbackServiceNameDeviceDelimit() + device,
				action);
    }
    
    @Override
    protected final void responsePreparation() {
    	Utility.addStrElementsWithHeaders(enterpriseOnboard._customer_id, customerId, output,
				params, device);
    }
    
    @Override
    protected final void deleteService() throws NavuException {
    	actionHandler.executeDeleteAction(customerId, device, serviceName, compositeKeyElems,
				params, maapi, th);
    }
    
    @Override
    protected final void diffOutput(String idx) throws ConfException, IOException {
    	actionHandler.executeDiffOutputAction(customerId, device, params, output, maapi, th,
				idx, EnterpriseOnboardErrorCodes.getErrorCodeDesc(idx),
				serviceName);
    }

 
	/**
     * Create (C) Customer Vrf service
     * 
     * @param params
     *            - an array of ConfXMLParam, parameters for completing the
     *            action
     */
    @Override
    protected final void createService() throws ConfException {

        LOGGER.info("Create " + serviceName + " Service: [" + customerName
                + "] customerId [" + customerId + "] device ["
                + device + "]");

        NavuContext ctx = new NavuContext(maapi, th);
        NavuList serviceList = new NavuContainer(ctx)
        .container(new Ncs().hash()).container(Ncs._services_)
        .list(serviceName, serviceName);

        NavuContainer serviceContainerInstance = serviceList
                .create(new ConfKey(this.compositeKeyElems));

        // Set parameters needed
        // Primary Parameters -- Key
        serviceContainerInstance.leaf(enterpriseOnboard._customer_id_).set(
                customerId);
        serviceContainerInstance.leaf(enterpriseOnboard._device_).set(device);

        // Secondary Parameters
        serviceContainerInstance.leaf(enterpriseOnboard._request_id_).set(
                requestId);
        serviceContainerInstance.leaf(enterpriseOnboard._customer_name_).set(
                customerName);
        serviceContainerInstance.leaf(enterpriseOnboard._rd_).set(rdVal);

       @SuppressWarnings("unchecked")
        ArrayList<String> array_rtImport = (ArrayList<String>) paramMap
        .get(enterpriseOnboard._rt_import_);
        String[] strrtImportList = new String[array_rtImport.size()];
        strrtImportList = array_rtImport.toArray(strrtImportList);

        int arrayLengthOfrtImport = array_rtImport.size();
        ConfList rtImportLeafList = new ConfList();

        for (int i = 0; i < arrayLengthOfrtImport; i++) {
              rtImportLeafList.addElem(new ConfUInt32(Long.parseLong(strrtImportList[i])));
        }
        serviceContainerInstance.leaf(enterpriseOnboard._rt_import_).set(
                rtImportLeafList);         

        serviceContainerInstance.leaf(enterpriseOnboard._rt_export_).set(
                rtExport);
        ConfEnumeration confEnum = new ConfEnumeration(enum_noCpe);
        serviceContainerInstance.leaf(enterpriseOnboard._no_cpe_).set(confEnum);

    }

    /**
     * Query (Retrieve) Customer Vrf service
     * 
     * @param params
     *            - an array of ConfXMLParam, parameters for completing the
     *            action
     * @throws NavuException 
     * @throws NSOException 
     */
    @Override
    protected final void queryService() throws NavuException, NSOException {

        LOGGER.info("Query " + serviceName + " Service: customerId ["
                + customerId + "]");

        NavuContext ctx = new NavuContext(maapi, th);
        NavuList serviceList = new NavuContainer(ctx)
        .container(new Ncs().hash()).container(Ncs._services_)
        .list(serviceName, serviceName);

        if (serviceList.containsNode(new ConfKey(compositeKeyElems))) {
            LOGGER.info("Query: " + serviceName
                    + " Service is available for query VRF [" + customerId
                    + "] Device [" + device + "]");
            NavuNode navuNode = serviceList.elem(keyElementsArray);

            Utility.addStrElementsWithHeaders(
                    enterpriseOnboard._customer_id, customerId, output,
                    params, device);

            String gotCustName = navuNode.leaf(
                    enterpriseOnboard._customer_name_).valueAsString();
            Utility.addStrElem(
                    enterpriseOnboard._customer_name, gotCustName, output);

            ConfUInt32 gotRd = (ConfUInt32) navuNode.leaf(
                    enterpriseOnboard._rd_).value();
            Utility.addInt32Elem(enterpriseOnboard._rd, gotRd,
                    output);

            ConfUInt32 gotRtExp = (ConfUInt32) navuNode.leaf(
                    enterpriseOnboard._rt_export_).value();
            Utility.addInt32Elem(enterpriseOnboard._rt_export,
                    gotRtExp, output);

           ConfList gotRtIxp = (ConfList) navuNode.leaf(
                    enterpriseOnboard._rt_import_).value();
            Utility.addListElem(enterpriseOnboard._rt_import,
                    gotRtIxp, output);

            ConfEnumeration gotNoCpe = (ConfEnumeration) (navuNode
                    .leaf(enterpriseOnboard._no_cpe_).value());
            Utility.addEnumElem(enterpriseOnboard._no_cpe,
                    gotNoCpe, output);
		} else {
			LOGGER.info("Query " + serviceName + " Service not available for query CustomerId [" 
		            + customerId + "] Device [" + device + "]");
			throw new NSOException("90006", "service not available for query");
		}
    }

}
