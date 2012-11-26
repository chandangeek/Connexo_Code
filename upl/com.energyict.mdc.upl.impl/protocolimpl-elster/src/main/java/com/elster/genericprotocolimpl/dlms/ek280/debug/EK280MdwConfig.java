package com.elster.genericprotocolimpl.dlms.ek280.debug;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.ShadowList;
import com.energyict.mdw.amr.RtuRegisterMapping;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.ProductSpecShadow;
import com.energyict.mdw.shadow.RtuTypeShadow;
import com.energyict.mdw.shadow.amr.RtuRegisterSpecShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpEntry;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpFile;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 30/11/11
 * Time: 11:11
 */
public class EK280MdwConfig {

    private static final String DEVICE_TYPE_NAME = "EK280 basic";

    private static final String[] OBIS_STRINGS = new String[]{
            "0.2.96.10.1.255",
            "0.0.96.10.2.255",
            "0.3.96.12.5.255",
            "0.3.96.12.6.255",
            "0.0.96.6.6.255",
            "7.0.0.2.13.255",
            "7.0.0.2.1.255",
            "7.0.0.2.0.255",
            "0.0.96.1.10.255",
            "7.0.96.99.3.255",
            "7.0.96.99.2.255",
            "7.0.0.2.14.255",
            "0.0.96.52.0.255",
            "7.0.0.9.4.255",
            "7.0.42.2.0.255",
            "7.0.41.2.0.255",
            "7.0.53.12.0.255",
            "7.0.0.4.0.255",
            "7.0.13.0.0.255",
            "7.0.13.2.0.255",
            "7.0.11.83.0.255",
            "7.0.11.83.1.255",
            "7.0.11.83.2.255",
            "7.0.11.83.3.255",
            "7.0.12.0.0.255",
            "7.0.12.81.1.255",
            "7.0.12.81.2.255",
            "7.0.12.81.3.255",
            "7.0.11.83.0.101",
            "7.0.11.83.1.101",
            "7.0.11.83.2.101",
            "7.0.11.83.3.101",
            "7.0.12.81.0.101",
            "7.0.12.81.1.101",
            "7.0.12.81.2.101",
            "7.0.12.81.3.101",
            "7.0.41.0.0.255",
            "7.0.42.0.0.255",
            "7.0.43.2.0.255",
            "7.0.43.0.0.255",
            "7.0.52.0.0.255",
            "7.0.53.0.0.255",
            "7.0.0.12.45.255",
            "7.0.0.12.46.255",
            "7.0.0.12.54.255",
            "7.0.0.12.60.255",
            "7.0.0.12.61.255",
            "7.0.0.12.65.255",
            "7.0.0.12.66.255",
            "7.0.0.12.67.255"
    };

    private MeteringWarehouse meteringWarehouse;

    public MeteringWarehouse mw() {
        if (meteringWarehouse == null) {
            if (MeteringWarehouse.getCurrent() == null) {
                MeteringWarehouse.createBatchContext();
            }
            meteringWarehouse = MeteringWarehouse.getCurrent();
        }
        return meteringWarehouse;
    }

    public static void main(String[] args) {
        //new EK280MdwConfig().doConfig();

        CommunicationDumpFile communicationDumpFile = new CommunicationDumpFile("C:\\EnergyICT\\workingdir\\labo\\debug\\Device 1_30nov11_153604.log");
        List<CommunicationDumpEntry> entries = communicationDumpFile.getEntries();
        System.out.println(communicationDumpFile.toString());
        System.out.println(communicationDumpFile.toStringAscii());

    }

    private void doConfig() {
        clearRegisterSpecs();

        for (String obisString : OBIS_STRINGS) {
            addRtuRegisterSpec(ObisCode.fromString(obisString));
        }

    }

    private void addRtuRegisterSpec(ObisCode obis) {
        try {
            RtuTypeShadow shadow = getRtuType().getShadow();
            ShadowList<RtuRegisterSpecShadow> registerSpecShadows = shadow.getRegisterSpecShadows();
            RtuRegisterSpecShadow registerSpecShadow = new RtuRegisterSpecShadow();
            registerSpecShadow.setRtuTypeId(getRtuType().getId());
            registerSpecShadow.setDeviceChannelIndex(obis.getB());
            registerSpecShadow.setDeviceObisCode(obis);
            registerSpecShadow.setLoadprofileChannelIndex(-1);
            registerSpecShadow.setNumberOfDigits(9);
            registerSpecShadow.setNumberOfFractionDigits(3);
            registerSpecShadow.setIntegral(false);
            registerSpecShadow.setOverflowValue(new BigDecimal("999999999"));
            registerSpecShadow.setRegisterMappingId(getRegisterMapping(obis).getId());

            registerSpecShadows.add(registerSpecShadow);

            getRtuType().update(shadow);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (BusinessException e) {
            System.out.println(e.getMessage());
        }
    }

    private RtuRegisterMapping getRegisterMapping(ObisCode obis) {
        RtuRegisterMapping mapping = mw().getRtuRegisterMappingFactory().find(obis, getUndefinedProductSpec().getId());
        if (mapping == null) {
            createRtuRegisterMapping(obis);
        }
        mapping = mw().getRtuRegisterMappingFactory().find(obis, getUndefinedProductSpec().getId());
        return mapping;
    }

    private void createRtuRegisterMapping(ObisCode obis) {

    }

    private ProductSpec getUndefinedProductSpec() {
        ProductSpec productSpec = mw().getProductSpecFactory().find(getUndefinedPhenomenon(), getAllTimeOfUse());
        if (productSpec == null) {
            createUndefinedProductSpec();
            productSpec = mw().getProductSpecFactory().find(getUndefinedPhenomenon(), getAllTimeOfUse());
        }
        return productSpec;
    }

    private void createUndefinedProductSpec() {
        try {
            ProductSpecShadow shadow = new ProductSpecShadow();
            shadow.setEdiCode("");
            shadow.setMeasurementCode("");
            shadow.setPhenomenonId(getUndefinedPhenomenon().getId());
            shadow.setTimeOfUseId(getAllTimeOfUse().getId());
            mw().getProductSpecFactory().create(shadow);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (BusinessException e) {
            System.out.println(e.getMessage());
        }
    }

    private Phenomenon getUndefinedPhenomenon() {
        return mw().getPhenomenonFactory().find(0);
    }

    private TimeOfUse getAllTimeOfUse() {
        return mw().getTimeOfUseFactory().find("All");
    }

    private void clearRegisterSpecs() {
        try {
            RtuTypeShadow shadow = getRtuType().getShadow();
            shadow.getRegisterSpecShadows().clear();
            getRtuType().update(shadow);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (BusinessException e) {
            System.out.println(e.getMessage());
        }
    }

    private DeviceType getRtuType() {
        return mw().getRtuTypeFactory().find(DEVICE_TYPE_NAME);
    }

}
