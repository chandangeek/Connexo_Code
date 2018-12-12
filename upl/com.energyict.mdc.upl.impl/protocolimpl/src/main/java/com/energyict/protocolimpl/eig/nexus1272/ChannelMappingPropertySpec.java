package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.protocolimpl.eig.nexus1272.parse.LinePoint;
import com.energyict.protocolimpl.properties.AbstractPropertySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-31 (14:45)
 */
class ChannelMappingPropertySpec extends AbstractPropertySpec {

    protected ChannelMappingPropertySpec(String name, boolean required, String displayName, String description) {
        super(name, required, displayName, description);
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

    @Override
    public ValueFactory getValueFactory() {
        return new ValueFactory();
    }

    private class ValueFactory implements com.energyict.mdc.upl.properties.ValueFactory {
        @Override
        public Object fromStringValue(String stringValue) {
            try {
                return parse(stringValue);
            } catch (InvalidPropertyException e) {
                e.printStackTrace(System.err);
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public String toStringValue(Object object) {
            return this.toStringValue((List<LinePoint>) object);
        }

        private String toStringValue(List<LinePoint> linePoints) {
            return linePoints.stream().map(this::toString).collect(Collectors.joining(","));
        }

        private String toString(LinePoint linePoint) {
            return linePoint.getChannel() + "=" + linePoint.getLine() + "." + linePoint.getPoint();
        }

        @Override
        public String getValueTypeName() {
            return com.energyict.mdc.upl.properties.LinePoint.class.getName();
        }

        @Override
        public Object valueToDatabase(Object object) {
            return this.toStringValue(object);
        }

        @Override
        public Object valueFromDatabase(Object databaseValue) {
            return this.fromStringValue((String) databaseValue);
        }
    }

}