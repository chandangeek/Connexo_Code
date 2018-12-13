package com.energyict.protocolimpl.modbus.schneider.powerlogic;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.generic.ParserFactory;
import com.energyict.protocolimpl.modbus.generic.RegisterDefinition;
import com.energyict.protocolimpl.modbus.generic.RegisterFactory;
import com.energyict.protocolimpl.modbus.generic.common.DataTypeSelector;

/**
 * @author sva
 * @since 18/03/2015 - 11:26
 */
public class PM5560RegisterFactory extends RegisterFactory {

    public static int OsFirmwareVersionAddress = 1637;
    public static int RsFirmwareVersionAddress = 1669;

    protected ParserFactory parserFactory;

    public PM5560RegisterFactory(Modbus modBus) {
        super(modBus);
    }

    @Override
    protected void init() {
        super.init();
        getRegisters().add(
                new HoldingRegister(OsFirmwareVersionAddress, 1, "Os firmware version")
                        .setParser(Integer.toString(DataTypeSelector.REGISTER_DATA_TYPE.getDataTypeCode()))
        );
        getRegisters().add(
                new HoldingRegister(RsFirmwareVersionAddress, 1, "Os firmware version")
                        .setParser(Integer.toString(DataTypeSelector.REGISTER_DATA_TYPE.getDataTypeCode()))
        );
//        getRegisters().add(
//                new HoldingRegister(CurrentDateTimeAddress, 4, "Present date & time")
//                        .setParser(Integer.toString(PM5560DataTypeSelector.DATE_TIME_DATA_TYPE.getDataTypeCode()))
//        );
//        getRegisters().add(
//                new HoldingRegister(SetDateTimeAddress, 4, "Set date & time")
//                        .setParser(Integer.toString(PM5560DataTypeSelector.DATE_TIME_DATA_TYPE.getDataTypeCode()))
//        );
//
//        getRegisters().add(
//                new HoldingRegister(UnprotectedCommandAddress, 2, "Unprotected command address")
//                        .setParser(Integer.toString(DataTypeSelector.UNKNOWN_DATA_TYPE.getDataTypeCode()))
//        );
//        getRegisters().add(
//                new HoldingRegister(UnprotectedCommandStatusAddress, 2, "Unprotected command status address")
//                        .setParser(Integer.toString(DataTypeSelector.REGISTER_DATA_TYPE.getDataTypeCode()))
//        );
//        getRegisters().add(
//                new HoldingRegister(UnprotectedCommandResultAddress, 2, "Unprotected command result address")
//                        .setParser(Integer.toString(DataTypeSelector.REGISTER_DATA_TYPE.getDataTypeCode()))
//        );
    }

    @Override
    public ParserFactory getParserFactory() {
        if (parserFactory == null) {
            parserFactory = new PM5560ParserFactory();
        }
        return parserFactory;
    }

    protected RegisterDefinition createRegisterDefinition(ObisCode obisCode) {
        return new PM5560RegisterDefinition(obisCode);
    }
}