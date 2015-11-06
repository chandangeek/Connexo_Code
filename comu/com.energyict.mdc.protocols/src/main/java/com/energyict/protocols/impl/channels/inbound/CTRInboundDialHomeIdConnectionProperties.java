package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for the {@link CTRInboundDialHomeIdConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:43)
 */
public class CTRInboundDialHomeIdConnectionProperties implements PersistentDomainExtension<ConnectionType> {

    public enum Fields {
        CONNECTION_TYPE {
            @Override
            public String javaName() {
                return "connectionType";
            }

            @Override
            public String databaseName() {
                return "CONNECTIONTYPE";
            }
        },
        DIAL_HOME_ID {
            @Override
            public String javaName() {
                return "dialHomeId";
            }

            @Override
            public String databaseName() {
                return "DIALHOMEID";
            }
        };

        public abstract String javaName();

        public abstract String databaseName();

    }

    @SuppressWarnings("unused")
    private Reference<ConnectionType> connectionType = Reference.empty();
    @SuppressWarnings("unused")
    private Interval interval;
    @NotEmpty
    @Size(max = Table.MAX_STRING_LENGTH)
    private String dialHomeId;

    @Override
    public void copyFrom(ConnectionType connectionType, CustomPropertySetValues propertyValues) {
        this.connectionType.set(connectionType);
        this.copyDialHomeId(propertyValues);
    }

    private void copyDialHomeId(CustomPropertySetValues propertyValues) {
        this.dialHomeId = (String) propertyValues.getProperty(Fields.DIAL_HOME_ID.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues) {
        propertySetValues.setProperty(Fields.DIAL_HOME_ID.javaName(), this.dialHomeId);
    }

}