package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Class to read the device clock
 *
 * Copyrights EnergyICT
 * Date: 20-apr-2011
 * Time: 14:10:28
 */
public class DateTime extends AbstractRegister {


    //Constructor to request all fields and registers
    public DateTime(Poreg poreg) {
        super(poreg, 0, 0, 1, 8);
    }

    //Constructor to request a custom number of fields and registers
    public DateTime(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    private Date time;

    public Date getTime() {
        return time;
    }

    public void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());
        this.time = DinTimeParser.parseValues(poreg, values);
    }

    public int getRegisterGroupID() {
        return RegisterGroupID.DateTime.getId();
    }
}