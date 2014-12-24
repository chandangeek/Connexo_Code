package com.energyict.mdc.engine.config;

public interface ComPortPoolMember {

    public OutboundComPortPool getComPortPool();

    public void setComPortPool(OutboundComPortPool comPortPoolReference);

    public ComPort getComPort();

    public void setComPort(OutboundComPort comPort);

}
