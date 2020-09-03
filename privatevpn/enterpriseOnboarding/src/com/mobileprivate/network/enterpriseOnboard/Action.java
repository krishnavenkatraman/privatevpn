/*
 * @copyright Cisco Systems, Inc 2016
 */
package com.mobileprivate.network.enterpriseOnboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

import com.cisco.as.nso.exception.ErrorStates;
import com.cisco.as.nso.exception.NSOException;
import com.cisco.as.nso.utility.Actions;
import com.cisco.as.nso.utility.Utility;
import com.mobileprivate.network.enterpriseOnboard.namespaces.enterpriseOnboard;
import com.tailf.cdb.CdbSession;
import com.tailf.conf.ConfBuf;
import com.tailf.conf.ConfException;
import com.tailf.conf.ConfNamespace;
import com.tailf.conf.ConfXMLParam;
import com.tailf.maapi.Maapi;
import com.tailf.navu.NavuException;

/**
 * Abstract actions API for NSO north bound return
 * 
 * @author name:Krishnaswamy Venkatraman
 * @author email:mukundnit@gmail.com
 */
public abstract class Action {

    protected ConfNamespace ns;
    protected ConfXMLParam[] params;

    protected String device;
    protected int action; // keep it for now till all code get moved
    protected ActionType actionEnum;
    protected String requestId;

    protected String url;
    protected Maapi maapi = null;
    protected int th = 0;
    protected CdbSession session = null;

    protected int errIdx;
    protected String errorCode;
    protected String errorMsg;
    protected List<ConfXMLParam> output;

    protected int numOfKeyElements;
    protected ConfBuf[] compositeKeyElems;
    protected String[] keyElementsArray;
    protected HashMap<String, Boolean> primaryArgs;
    protected HashMap<String, Boolean> secondaryArgs;
    protected String serviceName;

    protected HashMap<String, Object> paramMap;
    protected Actions actionHandler;
    protected static Logger LOGGER = Logger.getLogger(Action.class);

    /**
     * Constructor
     */
    public Action(ConfXMLParam[] paramList)
            throws NSOException {

        LOGGER.debug("Loading Input Parameter values in action constructor");

        paramMap = new HashMap<String, Object>();
        params = paramList;
        output = new ArrayList<ConfXMLParam>();
        action = Utility.getEnumStringParam(params,
                enterpriseOnboard._request_action_);
        actionEnum = ActionType.getByValue(action);
        
        // Setting up common namespaces used by the utility
        // It contains service specific field like _customer_id_, but since it's used in the 
        // common lib(should not), it has to be set here anyway. If project doesn't have such field, 
        // then null can be provided as value here.
        Utility.setCommonParamNameSpacesUtility(enterpriseOnboard.hash, enterpriseOnboard._request_id,
                enterpriseOnboard._request_action, enterpriseOnboard._device, enterpriseOnboard._code,
                enterpriseOnboard._message, enterpriseOnboard._diff_output, enterpriseOnboard._request_id_,
                enterpriseOnboard._request_action_, enterpriseOnboard._device_);
        Actions.setCommonParamNameSpacesAction(enterpriseOnboard._workflow_revert_,
                enterpriseOnboard._rollback_, enterpriseOnboard._service_, enterpriseOnboard._date_,
                enterpriseOnboard._customer_id_, enterpriseOnboard._action_);

        // Object to Check for Null or Empty Fields in Key Element parameters
        ErrorStates keyValidator = new ErrorStates();

        // Set Primary Parameters in Map
        primaryArgs = new HashMap<String, Boolean>();
        primaryArgs.put(enterpriseOnboard._request_id_, false);
        primaryArgs.put(enterpriseOnboard._request_action_, false);
        primaryArgs.put(enterpriseOnboard._device_, false);

        // Set Secondary Parameters in Map
        secondaryArgs = new HashMap<String, Boolean>();
        
        specifyRequiredActionParameters(params);

        // Evaluate Primary & Secondary ELements for Null & Missing values
        keyValidator.evaluatePayloadForMissingParams(action, params,
                primaryArgs, secondaryArgs);

        device = Utility.getStringParam(params,
                enterpriseOnboard._device_);
        requestId = Utility.getStringParam(params,
                enterpriseOnboard._request_id_);

        try {
            maapi = Utility.getNewMaapi();
            th = Utility.openMaapiWrite(maapi);
            session = Utility.getNewCdbOperSession(this.getClass());
        } catch (Exception e) {
            LOGGER.error(e);
            throw new NSOException(e);
        }
    }
    
    abstract void specifyRequiredActionParameters(ConfXMLParam[] params);
    protected abstract void createService() throws ConfException, NSOException;
    protected abstract void deleteService() throws NavuException;
    protected abstract void queryService() throws NSOException, ConfException;
    protected abstract void diffOutput(String idx) throws ConfException, IOException;
    protected abstract void saveRollback() throws NavuException;
    protected abstract void responsePreparation();
    
    public synchronized void serviceEventHandler(ConfXMLParam[] params) 
    	throws NSOException {

        try {
        	LOGGER.info("Starting service handling...");
        	
        	ErrorStates errorChecker = new ErrorStates();
            errorChecker.preLoadErrorValidation(action, device, maapi, th,
                    compositeKeyElems, serviceName, params,
                    primaryArgs, secondaryArgs);
            LOGGER.debug("Error Validation Completed");
        	
            switch (actionEnum) {
			case CREATE:
				LOGGER.debug("Calling Create Method");
				// Create the requested VRF
				createService();
				// Commit the change
				actionHandler.executeNCSCommitOperation(th, maapi, serviceName, action);
				// Save Transaction Rollback information
				saveRollback();
				// Add Output Header to Response
				responsePreparation();
				LOGGER.debug("Completed Create Action");
				break;
			case MODIFY:
				LOGGER.debug("Calling Modify Method");
				// For modification: all service parameters must be provided not
				// just parameters that are being changed/modified.
				deleteService();
				createService();
				// Commit the change
				actionHandler.executeNCSCommitOperation(th, maapi, serviceName, action);
				// Save Transaction Rollback information
				saveRollback();
				// Add Output Header to Response
				responsePreparation();
				LOGGER.debug("Completed Modify Action");
				break;
			case DELETE:
				LOGGER.debug("Calling Delete Method");
				// Delete VRF Service
				deleteService();
				// Commit the change
				actionHandler.executeNCSCommitOperation(th, maapi, serviceName, action);
				// Save Transaction Rollback information
				saveRollback();
				responsePreparation();
				LOGGER.debug("Completed Delete Action");
				break;
			case QUERY:
				LOGGER.debug("Calling Query Method");
				queryService();
				LOGGER.debug("Completed  Query Action");
				break;
			case CREATEDIFF:
				LOGGER.debug("Calling Create Diff Review Method");
				// Create the requested without applying it
				createService();
				diffOutput(EnterpriseOnboardErrorCodes.createReviewErrIndexNum);
				LOGGER.debug("Completed Create Diff Action");
				break;
			case MODIFYDIFF:
				LOGGER.debug("Calling Modify Diff Review Method");
				// Modify the requested Service without applying it
				deleteService();
				createService();
				diffOutput(EnterpriseOnboardErrorCodes.modifyReviewErrIndexNum);
				LOGGER.debug("Completed Modify Diff Action");
				break;
			case DELETEDIFF:
				LOGGER.debug("Calling Delete Diff Review Method");
				// Delete requested Service without applying it
				deleteService();
				diffOutput(EnterpriseOnboardErrorCodes.deleteReviewErrIndexNum);
				LOGGER.debug("Completed  Delete Diff Action");
				break;
			default:
				LOGGER.error("Received un-supported action enum: " + actionEnum);
				throw new NSOException(
						EnterpriseOnboardErrorCodes.unknownActionType,
						"Invalid or Unsupported Request Type: " + actionEnum);
			}
            
        } catch (ConfException | IOException e) {
            LOGGER.error(actionEnum + " failed: ", e);
            throw new NSOException(e);
		} finally {
			Utility.closeMaapiSock(maapi, LOGGER);
			Utility.closeCdbSession(session, LOGGER);
		}
    }
    
    protected static enum ActionType {
		// the numbers assigned here should match the implied numbers
		// on the YANG file for mpn-action-type
    	QUERY(0),
        CREATE(1),
        MODIFY(2),
        DELETE(3),
        CREATEDIFF(4),
        MODIFYDIFF(5),
        DELETEDIFF(6),
        ;
		private int action;
		ActionType(int action) {
			this.action = action;
		}
		
		int getAction() {
			return action;
		}
		
		static ActionType getByValue(int value) throws NSOException {
			for (ActionType val : ActionType.values()) {
				if (val.getAction() == value) {
					return val;
				}
			}
			throw new NSOException(EnterpriseOnboardErrorCodes.unknownActionType,
					  "ActionType enum not matched with YANG model for action " + value);
		}
    }

}
