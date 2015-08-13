package com.elster.protocolimpl.dlms.registers;

/**
 * User: heuckeg
 * Date: 04.01.13
 * Time: 08:35
 */
public interface IReadableRegister
{
    public String getObisCode();
    public String getDescriptor();
    public boolean match(String obisCode);
}
