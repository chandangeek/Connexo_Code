package com.energyict.mdc.protocol.api.device;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jbr
 * Date: 27-jul-2010
 * Time: 17:26:45
 */
public interface RegisterReadingRecord<R extends Register> {

    public int getRegisterId();

    public R getRegister();

    public BigDecimal getValue();

    public Date getReadTime();

    public Date getFromTime();

    public Date getToTime();

    public Date getEventTime();

    public String getText();

    public int getState();

    public int getReasonCode();

    public int getQualityCode();

    public BigDecimal getMultiplier();

    public BigDecimal getConsumptionAmount();

    public int getFlags();

    public boolean isStart();

    public boolean isStop();

}
