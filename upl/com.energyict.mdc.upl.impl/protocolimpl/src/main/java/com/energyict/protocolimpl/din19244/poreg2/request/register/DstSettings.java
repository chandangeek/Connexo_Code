package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.*;

import java.io.IOException;
import java.util.*;

/**
 * Class to read the DST settings.
 * These are used to determine if the device is in summer or winter time.
 *
 * Copyrights EnergyICT
 * Date: 12-mei-2011
 * Time: 9:49:35
 */
public class DstSettings extends AbstractRegister {

    public DstSettings(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.DstSettings.getId();
    }

    private Date start;
    private Date end;

    public Date getEnd() {
        return end;
    }

    public Date getStart() {
        return start;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.set(Calendar.MONTH, values.get(1).getValue() - 1);
        start.set(Calendar.DAY_OF_WEEK, values.get(3).getValue() + 1);
        start.set(Calendar.DAY_OF_MONTH, values.get(2).getValue());
        start.set(Calendar.HOUR_OF_DAY, 2);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.MONTH, values.get(6).getValue() - 1);
        end.set(Calendar.DAY_OF_WEEK, values.get(8).getValue() + 1);
        end.set(Calendar.DAY_OF_MONTH, values.get(7).getValue());
        end.set(Calendar.HOUR_OF_DAY, 3);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        this.start = start.getTime();
        this.end = end.getTime();
    }
}