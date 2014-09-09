package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.Checks;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return new Date(Long.valueOf(jsonValue));
    }

    @Override
    public String marshal(Date value) throws Exception {
        if (value==null) {
            return null;
        }
        return ""+value.getTime();
    }
}
