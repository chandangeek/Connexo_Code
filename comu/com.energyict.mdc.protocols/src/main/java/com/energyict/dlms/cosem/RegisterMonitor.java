

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class RegisterMonitor extends AbstractCosemObject {

    private Array thresholds=null;
    private Structure valueDefinition=null;
    private Array actions=null;

    /** Creates a new instance of Data */
    public RegisterMonitor(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    protected int getClassId() {
        return DLMSClassId.REGISTER_MONITOR.getClassId();
    }


    public Array readThresholds() throws IOException {
        if (thresholds == null) {
            thresholds = AXDRDecoder.decode(getLNResponseData(2)).getArray();
        }
        return thresholds;
    }
    public void writeThresholds(Array thresholds) throws IOException {
        write(2, thresholds.getBEREncodedByteArray());
        this.thresholds=thresholds;
    }

    public Structure readValueDefinition() throws IOException {
        if (valueDefinition == null) {
            valueDefinition = AXDRDecoder.decode(getLNResponseData(3)).getStructure();
        }
        return valueDefinition;
    }
    public void writeValueDefinition(Structure valueDefinition) throws IOException {
        write(3, valueDefinition.getBEREncodedByteArray());
    }


    public Array readActions() throws IOException {
        if (actions == null) {
            actions = AXDRDecoder.decode(getLNResponseData(4)).getArray();
        }
        return actions;
    }
    public void writeActions(Array actions) throws IOException {
        write(4, actions.getBEREncodedByteArray());
        this.actions=actions;
    }
}
