package com.energyict.dlms;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates the specific APDU tags. Depending on the used ciphering, a different tag is applied.
 *
 * Copyrights EnergyICT
 * Date: 26-apr-2010
 * Time: 11:53:49
 *
 */
public enum XdlmsApduTags {

    /** Different values */

    /* LongName requests */
    GET_REQUEST(DLMSCOSEMGlobals.COSEM_GETREQUEST, DLMSCOSEMGlobals.GLO_GETREQUEST, DLMSCOSEMGlobals.DED_GETREQUEST),
    SET_REQUEST(DLMSCOSEMGlobals.COSEM_SETREQUEST, DLMSCOSEMGlobals.GLO_SETREQUEST, DLMSCOSEMGlobals.DED_SETREQUEST),
    EVENT_NOTIFICATION_REQUEST(DLMSCOSEMGlobals.COSEM_EVENTNOTIFICATIONRESUEST, DLMSCOSEMGlobals.GLO_EVENTNOTIFICATION_REQUEST, DLMSCOSEMGlobals.DED_EVENTNOTIFICATION_REQUEST),
    ACTION_REQUEST(DLMSCOSEMGlobals.COSEM_ACTIONREQUEST, DLMSCOSEMGlobals.GLO_ACTIOREQUEST, DLMSCOSEMGlobals.DED_ACTIOREQUEST),

    /* LongName responses*/
    GET_RESPONSE(DLMSCOSEMGlobals.COSEM_GETRESPONSE, DLMSCOSEMGlobals.GLO_GETRESPONSE, DLMSCOSEMGlobals.DED_GETRESPONSE),
    SET_RESPONSE(DLMSCOSEMGlobals.COSEM_SETRESPONSE, DLMSCOSEMGlobals.GLO_SETRESPONSE, DLMSCOSEMGlobals.DED_SETRESPONSE),
    ACTION_RESPONSE(DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE, DLMSCOSEMGlobals.GLO_ACTIONRESPONSE, DLMSCOSEMGlobals.DED_ACTIONRESPONSE),

    /* ShortName requests*/
    READ_REQUEST(DLMSCOSEMGlobals.COSEM_READREQUEST, DLMSCOSEMGlobals.GLO_READREQUEST, DLMSCOSEMGlobals.DED_READREQUEST),
    WRITE_REQUEST(DLMSCOSEMGlobals.COSEM_WRITEREQUEST, DLMSCOSEMGlobals.GLO_WRITEREQUEST, DLMSCOSEMGlobals.DED_WRITEREQUEST),

    /* Shortname responses */
    READ_RESPONSE(DLMSCOSEMGlobals.COSEM_READRESPONSE, DLMSCOSEMGlobals.GLO_READRESPONSE, DLMSCOSEMGlobals.DED_READRESPONSE),
    WRITE_RESPONSE(DLMSCOSEMGlobals.COSEM_WRITERESPONSE, DLMSCOSEMGlobals.GLO_WRITERESPONSE, DLMSCOSEMGlobals.DED_WRITERESPONSE),

    /* Service errors */
    CONFIRMED_SERVICE_ERROR(DLMSCOSEMGlobals.COSEM_CONFIRMEDSERVICEERROR, DLMSCOSEMGlobals.GLO_CONFIRMEDSERVICEERROR, DLMSCOSEMGlobals.DED_CONFIRMEDSERVICEERROR),

    ;

    private byte unciphered;
    private byte globalCiphered;
    private byte dedicatedCiphered;

    /** Contains a list of possible {@link XdlmsApduTags} */
	private static Map<Byte, XdlmsApduTags> instances;

    /** Contains a list of possible global ciphered {@link com.energyict.dlms.XdlmsApduTags}*/
    private static Map<Byte, XdlmsApduTags> globalInstances;

    /** Contains a list of possible dedicated ciphered {@link com.energyict.dlms.XdlmsApduTags}*/
    private static Map<Byte, XdlmsApduTags> dedicatedInstances;

    /**
	 * Create for each uncipheredTag an entry in the instanceMap
	 * @return an instance map
	 */
	private static Map<Byte, XdlmsApduTags> getInstances() {
		if (instances == null) {
			instances = new HashMap<Byte, XdlmsApduTags>();
		}
		return instances;
	}

    /**
     * Create for each global cipheredTag an entry in the instanceMap
     * @return a global ciphered instance map
     */
    private static Map<Byte, XdlmsApduTags> getGlobalInstances() {
        if(globalInstances == null){
            globalInstances = new HashMap<Byte, XdlmsApduTags>();
        }
        return globalInstances;
    }

    /**
     * Create for each dedicated cipheredTag an entry in the instanceMap
     * @return a dedicated ciphered instance map
     */
    private static Map<Byte, XdlmsApduTags> getDedicatedInstances() {
        if(dedicatedInstances == null){
            dedicatedInstances = new HashMap<Byte, XdlmsApduTags>();
        }
        return dedicatedInstances;
    }
    /**
     * private constructor initializing the different tags
     *
     * @param unciphered
     *              - The Unciphered tag
     * @param globalCiphered
     *              - The global ciphered tag
     * @param dedicatedCiphered
     *              - The dedicated ciphering tag
     */
    private XdlmsApduTags(byte unciphered, byte globalCiphered, byte dedicatedCiphered) {
        this.unciphered = unciphered;
        this.globalCiphered = globalCiphered;
        this.dedicatedCiphered = dedicatedCiphered;
        getInstances().put(unciphered, this);
        getGlobalInstances().put(globalCiphered, this);
        getDedicatedInstances().put(dedicatedCiphered, this);
    }

    /**
     * Return the encrypted tag depending on whether it is a global or a dedicated session
     *
     * @param global
     *          - true if it is a global session
     *
     * @return the corresponding encrypted tag
     */
    public static byte getEncryptedTag(byte unencrypted, boolean global){
        XdlmsApduTags tag = getInstances().get(unencrypted);
        if(global){
            return tag.getGlobalCipheredTag();
        } else {
            return tag.getDedicatedCipheredTag();
        }
    }

    /**
     * @return the global ciphering tag
     */
    protected byte getGlobalCipheredTag(){
        return this.globalCiphered;
    }

    /**
     * @return the dedicated ciphering tag
     */
    protected byte getDedicatedCipheredTag(){
        return this.dedicatedCiphered;
    }

    /**
     * Checks whether the given tag is known to our implementation
     *
     * @param cipheredTag
     *          - the given ciphered tag
     *
     * @return true if we know which ciphered tag it is, otherwise false
     */
    public static boolean contains(byte cipheredTag) {
        if(getDedicatedInstances().containsKey(cipheredTag)) {
            return true;
        } else if (getGlobalInstances().containsKey(cipheredTag)) {
            return true;
        }
        return false;
    }
}
