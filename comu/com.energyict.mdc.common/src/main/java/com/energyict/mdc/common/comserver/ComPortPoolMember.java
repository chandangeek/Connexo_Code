/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Link between {@link OutboundComPort} and {@link OutboundComPortPool}.
 * In other words, keept track of which ComPort is part of which OutboundComPortPool.
 */
public interface ComPortPoolMember {

    public OutboundComPortPool getComPortPool();

    public void setComPortPool(OutboundComPortPool comPortPoolReference);

    public OutboundComPort getComPort();

    public void setComPort(OutboundComPort comPort);

}
