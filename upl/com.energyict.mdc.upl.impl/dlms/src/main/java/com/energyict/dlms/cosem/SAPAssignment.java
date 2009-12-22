/*
 * SAPAssignment.java
 *
 * Created on 30 augustus 2004, 11:19
 */

package com.energyict.dlms.cosem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ProtocolLink;

/**
 *
 * @author  Koen
 */
public class SAPAssignment extends AbstractCosemObject {
    public final int DEBUG=0;
    List logicalDeviceNames=null; 
    /** Creates a new instance of SAPAssignment */
    public SAPAssignment(ProtocolLink protocolLink) {
        super(protocolLink,protocolLink.getReference() == ProtocolLink.LN_REFERENCE?new ObjectReference(SAP_OBJECT_LN):new ObjectReference(SAP_OBJECT_SN));
    }
    
    protected int getClassId() {
        return DLMSClassId.SAP_ASSIGNMENT.getClassId();
    }
    
    public List getLogicalDeviceNames() throws IOException {
        if (logicalDeviceNames==null) {
            logicalDeviceNames = new ArrayList();
            DataContainer dataContainer = new DataContainer();
            dataContainer.parseObjectList(getResponseData(SAP_ATTR_ASSIGNMENT_LIST),protocolLink.getLogger());
            if (DEBUG >= 1) {
				dataContainer.printDataContainer();
			}
            for (int i=0;i<dataContainer.getRoot().getNrOfElements();i++) {
                DataStructure ds = (DataStructure)dataContainer.getRoot().getElement(i);
                for (int t=0;t<ds.getNrOfElements();t++) {
                   if (ds.isOctetString(t)) {
                       OctetString octetString = ds.getOctetString(t);
                       logicalDeviceNames.add(octetString.toString().trim());
                   }
//                   else {
                       // get all other fields of the structure which is instance specific.
                       // e.g. the Siemens returns beside the logical address name also a
                       // SAP identification 12xxxx (where xxxx is the SAP identification
//                   }
                }
            }
        }
        return logicalDeviceNames;
    }
    
}
