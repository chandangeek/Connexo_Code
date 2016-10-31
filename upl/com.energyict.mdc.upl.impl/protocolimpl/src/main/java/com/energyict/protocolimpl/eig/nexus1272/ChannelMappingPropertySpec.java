package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocolimpl.eig.nexus1272.parse.LinePoint;
import com.energyict.protocolimpl.properties.AbstractPropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (14:45)
 */
class ChannelMappingPropertySpec extends AbstractPropertySpec {

    protected ChannelMappingPropertySpec(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean validateValue(Object value) throws PropertyValidationException {
        if (this.isRequired() && value == null) {
            throw MissingPropertyException.forName(this.getName());
        } else if (value instanceof String) {
            parse((String) value);
            return true;
        } else {
            throw InvalidPropertyException.forNameAndValue(this.getName(), value);
        }
    }

    static List<LinePoint> parse(String channelMapping) throws InvalidPropertyException {
        List <LinePoint> ret = new ArrayList<>();
        String[] lpStrs = channelMapping.split(",");
        for (int i = 0; i<lpStrs.length; i++) {
            String[] chnLPStr = lpStrs[i].split("=");
            if (chnLPStr.length != 2) {
                throw new InvalidPropertyException("Malformed Nexus Channel Mapping custom property");
            }
            int chan = Integer.parseInt(chnLPStr[0]);
            String toSplit = chnLPStr[1];
            String[] lpStr = toSplit.split("\\.");
            if (lpStr.length != 2) {
                throw new InvalidPropertyException("Malformed Nexus Channel Mapping custom property");
            }
            LinePoint lp = new LinePoint(Integer.parseInt(lpStr[0]), Integer.parseInt(lpStr[1]), chan);
            ret.add(lp);
        }
        return ret;
    }
}