package com.elster.protocolimpl.dlms;

import com.elster.dlms.cosem.applicationlayer.CosemDataAccessException;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleClockObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import com.elster.protocolimpl.dlms.messaging.A1MessageExecutor;
import com.elster.protocolimpl.dlms.messaging.DlmsMessageExecutor;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.A1ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.IReadWriteObject;
import com.elster.protocolimpl.dlms.objects.a1.utils.BPValueHist;
import com.elster.protocolimpl.dlms.objects.a1.utils.BillingProfileReader;
import com.elster.protocolimpl.dlms.objects.a1.utils.ExtendedRegisterReader;
import com.elster.protocolimpl.dlms.objects.a1.utils.HistoricRegisterResult;
import com.elster.protocolimpl.dlms.objects.a1.utils.RegisterReader;
import com.elster.protocolimpl.dlms.profile.ArchiveProcessorFactory;
import com.elster.protocolimpl.dlms.profile.DlmsProfile;
import com.elster.protocolimpl.dlms.profile.ILogProcessor;
import com.elster.protocolimpl.dlms.util.A1Defs;
import com.elster.protocolimpl.dlms.util.A1Utils;
import com.elster.protocolimpl.dlms.util.DlmsUtils;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * User: heuckeg
 * Date: 22.09.11
 * Time: 14:42
 */
@SuppressWarnings({"unused"})
public class A1 extends Dlms {

    // property name for scaler
    private final String PROP_SCALERVALUE = "ScalerValue";
    //
    private final static String OBISCODE_TST = "0.0.1.0.0.255";

    protected static final ObisCode OBISCODE_60MPROFILE = new ObisCode(7, 0, 99, 99, 2, 255);

    protected static String ARCHIVESTRUCTURE_V2 = "TST=" + OBISCODE_TST + "," +
            "CHN0[C9]=7.0.13.83.0.255" + "," +
            "CHN1[C9]=7.0.12.81.0.255" + "," +
            "EVT_DLMS=0.2.96.10.1.255";

    protected static String ARCHIVESTRUCTURE_V3 = "TST=" + OBISCODE_TST + "," +
            "CHN0[C9U:14]=7.0.13.83.0.255" + "," +
            "CHN1[C9U:14]=7.0.12.82.0.255" + "," +
            "EVT_DLMSV3=0.0.96.10.1.255";

    protected static String ARCHIVESTRUCTURE_V4 = "TST=" + OBISCODE_TST + "," +
            "CHN0[C9U:14]=7.0.13.83.0.255" + "," +
            "CHN1[C9U:14]=7.0.12.82.0.255" + "," +
            "EVT_DLMSV4=0.0.96.10.1.255";

    // event log 7.0.99.98.1.255
    protected static final ObisCode LOG_OC = new ObisCode(7, 0, 99, 98, 1, 255);

    protected static final String LOGSTRUCTURE_V2 = "TST=" + OBISCODE_TST + "," +
            "EVT_UMI1=0.0.96.15.7.255";

    protected static final String LOGSTRUCTURE_V3 = "TST=" + OBISCODE_TST + "," +
            "EVT_UNITS1=0.0.96.11.1.255";

    /* version as number */
    private int a1Version = -1;
    /* object pool for A1 (managing version depended objects */
    private A1ObjectPool objectPool;
    /* scaler */
    private String globalScaler = null;
    private BillingProfileReader billingProfileReader = null;

    public A1() {
        super();

        objectPool = new A1ObjectPool();

        ocIntervalProfile = OBISCODE_60MPROFILE;
        ocLogProfile = LOG_OC;
    }

    /**
     * This is the protocol version, automatically updated after every commit by svn
     *
     * @return The version
     */
    public String getProtocolVersion() {
        return "$Date: 2014-09-17 09:03:19 +0200 (wo, 17 sep 2014) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        if (firmwareVersion.length() > 0) {
            return firmwareVersion;
        }
        final CosemAttributeDescriptor value = new CosemAttributeDescriptor(new ObisCode("7.0.0.2.1.255"),
                CosemClassIds.DATA, 2);
        DlmsData data = connection.getApplicationLayer().getAttributeAndCheckResult(value);
        firmwareVersion = ((DlmsDataVisibleString) data).getValue();
        return firmwareVersion;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected List doGetOptionalKeys() {
        ArrayList<String> optionalKeys = new ArrayList<String>();
        optionalKeys.add(PROP_SCALERVALUE);
        return optionalKeys;
    }

    @Override
    protected void validateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {
        super.validateProperties(properties);

        archiveStructure = properties.getProperty(Dlms.ARCHIVESTRUCTURE, "");

        logStructure = properties.getProperty(Dlms.LOGSTRUCTURE, "");

        String s = properties.getProperty(PROP_SCALERVALUE, "");
        if (s.length() > 0) {
            globalScaler = s;
        }
    }

    @Override
    public void setTime()
            throws IOException {
        SimpleCosemObjectManager objectManager = getObjectManager();
        SimpleClockObject clockObject =
                objectManager.getSimpleCosemObject(A1Defs.CLOCK_OBJECT, SimpleClockObject.class);
        DlmsDateTime dt = new DlmsDateTime(new Date());
        clockObject.setTime(dt);
    }

    public SimpleCosemObjectManager getObjectManager() {
        if (objectManager == null) {
            getLogger().info("getObjectManager: creating SimpleCosemObjectManager");
            objectManager = new SimpleCosemObjectManager(getDlmsConnection().getApplicationLayer(), A1Defs.DEFINITIONS);
            getLogger().info("getObjectManager: creating SimpleCosemObjectManager ended");
        }
        return objectManager;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents)
            throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range for A1 set to 1 month - 7/4/2013 gh */
        calendar.add(Calendar.MONTH, -1);
        return getProfileData(calendar.getTime(), includeEvents);
    }


    @Override
    protected DlmsProfile getProfileObject()
            throws IOException {
        if (intervalProfile == null) {
            getLogger().info("getProfileObject: creating profile object");
            SimpleProfileObject profileObject = (SimpleProfileObject) getObjectManager().getSimpleCosemObject(ocIntervalProfile);

            // if archive structure not defined "outside"...
            if (archiveStructure.length() == 0) {
                if (getSwVersion() == 0) {
                    throw new IOException("getProfileObject: Can't set archive structure (version = 0)");
                }
                if (getSwVersion() < 0x011539) {
                    archiveStructure = ARCHIVESTRUCTURE_V2;
                } else {
                    String scalerString = null;
                    if (globalScaler == null) {
                        Integer s = A1Utils.calculateScalerFromType(connection.getApplicationLayer());
                        if (s != null) {
                            scalerString = "" + s;
                        }
                    } else {
                        scalerString = globalScaler;
                    }

                    if (getSwVersion() < 0x011549) {
                        archiveStructure = ARCHIVESTRUCTURE_V3;
                    } else {
                        archiveStructure = ARCHIVESTRUCTURE_V4;
                    }

                    if (scalerString != null) {
                        archiveStructure += "|S:" + scalerString;
                    }
                }
            }
            getLogger().info("getProfileObject: creating dlms profile");
            intervalProfile = new DlmsProfile(this, "A1V1", archiveStructure, profileObject);
            getLogger().info("getProfileObject: creating object ended");

        }
        return intervalProfile;
    }

    protected ILogProcessor getLogProfileObject()
            throws IOException {
        if (logProfile == null) {

            SimpleProfileObject profileObject = (SimpleProfileObject) getObjectManager().getSimpleCosemObject(ocLogProfile);

            if ((logStructure == null) || (logStructure.length() == 0)) {
                if (getSwVersion() == 0) {
                    throw new IOException("getLogProfileObject: Can't set archive structure (version = 0)");
                }
                if (getSwVersion() < 0x011539) {
                    logStructure = LOGSTRUCTURE_V2;
                } else {
                    logStructure = LOGSTRUCTURE_V3;
                }
            }
            logProfile = ArchiveProcessorFactory.createLogProcessor("A1V1", logStructure, profileObject, getTimeZone(), getLogger());
        }
        return logProfile;
    }

    @Override
    public DlmsMessageExecutor getMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = new A1MessageExecutor(this);
        }
        return messageExecutor;
    }

    @Override
    public ObjectPool getObjectPool() {
        return objectPool;
    }

    @Override
    public int getSoftwareVersion() {
        return getSwVersion();
    }


    /*
     * A1 driver overrides readRegister due to great version dependencies for register reading!
     */
    public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode)
            throws IOException {
        getLogger().info("readRegister: " + obisCode.toString());
        ObisCode oc = new ObisCode(obisCode.toString());
        IReadWriteObject rwo = objectPool.findByCode(getSwVersion(), oc);
        if (rwo == null) {
            getLogger().warning(obisCode.toString() + ": not found in object pool");
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported by the device/version");
        }

        Object o;
        try {
            if (rwo instanceof BPValueHist) {
                ((BPValueHist) rwo).setBillingProfileReader(getBillingProfileReader());
            }
            o = rwo.read(connection.getApplicationLayer());
        } catch (CosemDataAccessException ex) {

            getLogger().warning(obisCode.toString() + ": cosem data access error " + ex.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported by the device/version");
        } catch (IOException ex) {
            String msg = ex.getMessage();
            if (msg == null) {
                msg = ex.getClass().getName();
            } else {
                if (msg.equalsIgnoreCase("no value")) {
                    getLogger().warning(obisCode.toString() + ": no value");
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported by the device/version");
                }
            }
            getLogger().warning(obisCode.toString() + ": IOException (" + msg + ")");
            throw ex;
        }

        RegisterValue result;
        //getLogger().info(obisCode.toString() + ": result is of type " + o.getClass().getName());
        if (o instanceof String) {
            result = new RegisterValue(obisCode, (String) o);
        } else if (o instanceof RegisterReader.RegisterResult) {
            RegisterReader.RegisterResult rr = (RegisterReader.RegisterResult) o;
            Unit unitCode = DlmsUtils.getUnitFromDlmsUnit(rr.getUnit());
            Quantity val = new Quantity(rr.getValue(), unitCode);

            result = new RegisterValue(obisCode, val, null, new Date());
        } else if (o instanceof HistoricRegisterResult) {
            HistoricRegisterResult hrr = (HistoricRegisterResult) o;

            Object r = hrr.registerResult();
            if (r instanceof String) {
                result = new RegisterValue(obisCode, null, null, hrr.getFromDate(), hrr.getToDate(), hrr.getReadDate(), 0, r.toString());
            } else if (r instanceof Number) {
                Quantity val = new Quantity((Number) r, Unit.get(BaseUnit.NOTAVAILABLE));
                result = new RegisterValue(obisCode, val, null, hrr.getFromDate(), hrr.getToDate(), hrr.getReadDate());
            } else if (r instanceof RegisterReader.RegisterResult) {
                RegisterReader.RegisterResult rr = (RegisterReader.RegisterResult) r;
                Unit unitCode = DlmsUtils.getUnitFromDlmsUnit(rr.getUnit());
                Quantity val = new Quantity(rr.getValue(), unitCode);
                result = new RegisterValue(obisCode, val, null, hrr.getFromDate(), hrr.getToDate(), hrr.getReadDate());
            } else if (r instanceof ExtendedRegisterReader.ExtendedRegisterResult) {
                ExtendedRegisterReader.ExtendedRegisterResult rr = (ExtendedRegisterReader.ExtendedRegisterResult) r;
                Unit unitCode = DlmsUtils.getUnitFromDlmsUnit(rr.getUnit());
                Quantity val = new Quantity(rr.getValue(), unitCode);
                result = new RegisterValue(obisCode, val, rr.getDate(), hrr.getFromDate(), hrr.getToDate(), hrr.getReadDate());
            } else {
                result = new RegisterValue(obisCode, null, null, hrr.getFromDate(), hrr.getToDate(), hrr.getReadDate(), 0, r.toString());
            }
        } else {
            result = new RegisterValue(obisCode, o.toString());
        }

        getLogger().info(result.toString());
        return result;
    }

    private BillingProfileReader getBillingProfileReader() {
        if (billingProfileReader == null) {
            billingProfileReader = new BillingProfileReader(connection.getApplicationLayer(), getLogger());
        }
        return billingProfileReader;
    }


    protected int getSwVersion() {
        if (a1Version >= 0) {
            return a1Version;
        }

        a1Version = 0;
        try {
            String fwVersion = getFirmwareVersion();
            String part[] = fwVersion.split("[.]");
            for (String aPart : part) {
                a1Version = (a1Version << 8) + Byte.parseByte(aPart);
            }
            return a1Version;
        } catch (IOException e) {
            return a1Version;
        }
    }
}