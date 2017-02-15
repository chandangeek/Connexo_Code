

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned16;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class ScriptTable extends AbstractCosemObject {

	/** Attributes */
    private Array scripts=null;

    /** Attribute numbers */
    private static final int ATTRB_SCRIPTS = 2;

    /** Methods */
    private static final int EXECUTE_SCRIPT = 1;

	public static final byte[] LN_GLOBAL_METER_RESET = new byte[]{0,0,10,0,0,(byte)255};
	public static final byte[] LN_MDI_RESET = new byte[]{0,0,10,0,1,(byte)255};
	public static final byte[] LN_TARIFFICATION_SCRIPT_TABLE = new byte[]{0,0,10,0,100,(byte)255};
	public static final byte[] LN_ACTIVATE_TEST_MODE = new byte[]{0,0,10,0,101,(byte)255};
	public static final byte[] LN_ACTIVATE_NORMAL_MODE = new byte[]{0,0,10,0,102,(byte)255};
	public static final byte[] LN_SET_OUTPUT_SIGNALS = new byte[]{0,0,10,0,103,(byte)255};
	public static final byte[] LN_SWITCH_OPTICAL_TEST_OUTPUT = new byte[]{0,0,10,0,104,(byte)255};
	public static final byte[] LN_POWER_QUALITY_MANAGEMENT = new byte[]{0,0,10,0,105,(byte)255};
	public static final byte[] LN_DISCONNECT_CONTROL = new byte[]{0,0,10,0,106,(byte)255};
	public static final byte[] LN_IMAGE_ACTIVATION = new byte[]{0,0,10,0,107,(byte)255};
	public static final byte[] LN_BROADCAST_SCRIPT_TABLE = new byte[]{0,0,10,0,125,(byte)255};

    /** Creates a new instance of Data */
    public ScriptTable(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

	public ScriptTable(ProtocolLink protocolLink, byte[] scriptType) {
        super(protocolLink,new ObjectReference(scriptType));
    }

	public static ObisCode getGlobalResetObisCode() {
		return ObisCode.fromByteArray(LN_GLOBAL_METER_RESET);
	}

    protected int getClassId() {
        return DLMSClassId.SCRIPT_TABLE.getClassId();
    }

    public void writeScripts(Array scripts) throws IOException {
        write(ATTRB_SCRIPTS, scripts.getBEREncodedByteArray());
    }
    public Array readScripts() throws IOException {
        if (scripts == null) {
            scripts = (Array) AXDRDecoder.decode(getLNResponseData(2));
        }
        return scripts;
    }

    public void execute(int data) throws IOException {
        Unsigned16 u16 = new Unsigned16(data);
        invoke(EXECUTE_SCRIPT,u16.getBEREncodedByteArray());
    }

}
