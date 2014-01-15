package com.energyict.dynamicattributes;

import java.util.Date;

public abstract class AbstractDateFactory extends AbstractValueFactory<Date> {

    public abstract boolean isUtcTimeStamp();

}