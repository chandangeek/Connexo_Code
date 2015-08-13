package com.elster.protocolimpl.dlms.registers;

/**
 * User: heuckeg
 * Date: 03.01.13
 * Time: 13:37
 */
public class DlmsHistoricalRegisterDefinition
    implements IReadableRegister
{

    private final HistoricalObisCode hoc;

    private final String profile;
    private final String ocValue;
    private final int atValue;
    private final String ocDate;
    private final int atDate;

    public DlmsHistoricalRegisterDefinition(final HistoricalObisCode code, final String profile,
                                            final String ocValue, final int atValue,
                                            final String ocDate, final int atDate)
    {
        this.hoc = code;
        this.profile = profile;
        this.ocValue = ocValue;
        this.atValue = atValue;
        this.ocDate = ocDate;
        this.atDate = atDate;
    }

    public com.elster.dlms.types.basic.ObisCode getProfile()
    {
        return new com.elster.dlms.types.basic.ObisCode(profile);
    }

    public com.elster.dlms.types.basic.ObisCode getOcValue()
    {
        return new com.elster.dlms.types.basic.ObisCode(ocValue);
    }

    public com.elster.dlms.types.basic.ObisCode getOcDate()
    {
        return new com.elster.dlms.types.basic.ObisCode(ocDate);
    }

    public int getAtValue()
    {
        return atValue;
    }

    public int getAtDate()
    {
        return atDate;
    }

    public String getObisCode()
    {
        return hoc.getObisCode();
    }

    public String getDescriptor()
    {
        return hoc.getDesc();
    }

    public boolean match(String obisCode)
    {
        return obisCode.matches(this.hoc.getObisCode());
    }
}
