package com.energyict.mdc.engine.model;

public interface ComPortPoolMember {

    public ComPortPool getComPortPool();

    public void setComPortPool(ComPortPool comPortPoolReference);

    public ComPort getComPort();

    public void setComPort(ComPort comPort);

    public void remove();
}
