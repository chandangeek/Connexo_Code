package com.energyict.mdc.device.data.ami;
import com.elster.jupiter.util.units.Quantity;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

public class ContactorInfo {

    public BreakerStatus status;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant activationDate;
    public Quantity loadLimit;
    public String readingType;
    public String callback;

    @Override
    public String toString() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("ContactorInfo{");
        msgBuilder.append("status: ").append(status.getDescription());
        if (activationDate != null) {
            msgBuilder.append(", activationDate: ").append(activationDate);
        }
        if (loadLimit != null) {
            msgBuilder.append(", loadLimit: ").append(loadLimit);
        }
        if (readingType != null) {
            msgBuilder.append(", readingType: ").append(readingType);
        }
        msgBuilder.append(", callBack: ").append(callback);
        msgBuilder.append("}");
        return msgBuilder.toString();
    }

    public void shouldDisableLoadLimit() {
        //TODO - better
        loadLimit = null;
    }


}