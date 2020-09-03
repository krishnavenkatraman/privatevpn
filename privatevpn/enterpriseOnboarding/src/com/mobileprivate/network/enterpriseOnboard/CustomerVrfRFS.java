/*
 * Licensed to Cisco Systems, Inc
 */
package com.mobileprivate.network.enterpriseOnboard;

import com.tailf.conf.ConfList;
import com.tailf.conf.ConfObject;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;

import com.cisco.as.nso.utility.Utility;
import com.mobileprivate.network.enterpriseOnboard.namespaces.enterpriseOnboard;
import com.tailf.conf.ConfException;
import com.tailf.dp.annotations.ServiceCallback;
import com.tailf.dp.proto.ServiceCBType;
import com.tailf.dp.services.ServiceContext;
import com.tailf.navu.NavuNode;

/**
 * Represents Customer Vrf configuration on the device
 * 
 * @author name:Krishnaswamy Venkatraman
 * @author email:mukundnit@gmail.com
 *
 */
public class CustomerVrfRFS {

    private static Logger LOGGER = Logger.getLogger(CustomerVrfRFS.class);
    /**
     * Create callback method. This method is called when a service instance
     * committed due to a create or update event.
     *
     * This method returns a opaque as a Properties object that can be null. If
     * not null it is stored persistently by Ncs. This object is then delivered
     * as argument to new calls of the create method for this service (fastmap
     * algorithm). This way the user can store and later modify persistent data
     * outside the service model that might be needed.
     *
     * @param context
     *            - The current ServiceContext object
     * @param service
     *            - The NavuNode references the service node.
     * @param ncsRoot
     *            - This NavuNode references the ncs root.
     * @param opaque
     *            - Parameter contains a Properties object. This object may be
     *            used to transfer additional information between consecutive
     *            calls to the create callback. It is always null in the first
     *            call. I.e. when the service is first created.
     * @return Properties the returning opaque instance
     * @throws ConfException
     */

    @ServiceCallback(servicePoint = enterpriseOnboard.servicepoint_customer_vrf_servicepoint, callType = ServiceCBType.CREATE)
    public Properties create(ServiceContext context, NavuNode service,
            NavuNode ncsRoot, Properties opaque) throws ConfException {

        String givenTemplate = null;
        HashMap<String, String> custVrfMap = null;
        Utility utlityHandler = null;

        utlityHandler = new Utility();

        if (service.leaf(enterpriseOnboard._no_cpe_).valueAsString() == null
                || service.leaf(enterpriseOnboard._no_cpe_).valueAsString()
                .equalsIgnoreCase("yes")) {
            givenTemplate = "asr1k-cust-vrf-nocpe-templ";
        } else {
            givenTemplate = "asr1k-cust-vrf-templ";
        }

        custVrfMap = new HashMap<String, String>();

        custVrfMap.put("DEVICE", service.leaf(enterpriseOnboard._device_)
                .valueAsString());
        custVrfMap
        .put("CUST-NAME",
                service.leaf(enterpriseOnboard._customer_name_)
                .valueAsString());
        custVrfMap.put("CUST-ID", service.leaf(enterpriseOnboard._customer_id_)
                .valueAsString());
        custVrfMap.put("RD-VAL", service.leaf(enterpriseOnboard._rd_)
                .valueAsString());
        custVrfMap.put("RT-EXPORT", service.leaf(enterpriseOnboard._rt_export_)
                .valueAsString());


        ConfList rtImportLeafList = (service.leaf(enterpriseOnboard._rt_import_).value() != null)
                ?(ConfList) service.leaf(enterpriseOnboard._rt_import_).value(): new ConfList();

        for (ConfObject rtImportLeafListElement : rtImportLeafList.elements()) {
            if (rtImportLeafListElement.toString() != "") {
            custVrfMap.put("RT-IMPORT", rtImportLeafListElement.toString());
            }
            utlityHandler.applyDeviceConfigIntoTemplates(givenTemplate,service, context, custVrfMap);
        }
   
        LOGGER.info("Finished Applying Device Config template for Customer Vrf");

        return opaque;
    }
}
