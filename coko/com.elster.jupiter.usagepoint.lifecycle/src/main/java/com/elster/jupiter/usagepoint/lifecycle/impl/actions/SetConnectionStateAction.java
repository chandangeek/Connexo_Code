package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultTransition;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SetConnectionStateAction extends TranslatableAction {
    private static final Function<ConnectionState, String> KEY_EXTRACTOR = Enum::name;
    private static final Function<String, ConnectionState> OBJECT_FETCHER = ConnectionState::valueOf;
    private final PropertySpecService propertySpecService;
    private final IdWithNameFactory<ConnectionState> connectionStateFactory;

    @Inject
    public SetConnectionStateAction(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
        this.connectionStateFactory = new IdWithNameFactory<>(KEY_EXTRACTOR, this::translateConnectionState, OBJECT_FETCHER);
    }

    @Override
    public String getCategory() {
        return MicroCategory.CONNECTION_STATE.name();
    }

    @Override
    protected Set<DefaultTransition> getTransitionCandidates() {
        return EnumSet.of(DefaultTransition.INSTALL_ACTIVE, DefaultTransition.INSTALL_INACTIVE);
    }

    private String translateConnectionState(ConnectionState connectionState) {
        if (connectionState == null) {
            return null;
        }
        return getThesaurus().getString(connectionState.getKey(), connectionState.getDefaultFormat());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(this.propertySpecService.specForValuesOf(this.connectionStateFactory)
                .named(MicroActionTranslationKeys.SET_CONNECTION_STATE_PROPERTY_NAME)
                .fromThesaurus(getThesaurus())
                .addValues(getPossibleValues())
                .markRequired()
                .markExhaustive()
                .finish());
    }

    private List<HasIdAndName> getPossibleValues() {
        return Arrays.asList(
                this.connectionStateFactory.wrap(ConnectionState.CONNECTED),
                this.connectionStateFactory.wrap(ConnectionState.PHYSICALLY_DISCONNECTED),
                this.connectionStateFactory.wrap(ConnectionState.LOGICALLY_DISCONNECTED));
    }

    @Override
    protected void doExecute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) {
        Object wrappedConnectionState = properties.get(MicroActionTranslationKeys.SET_CONNECTION_STATE_PROPERTY_NAME.getKey());
        if (wrappedConnectionState == null || !(wrappedConnectionState instanceof HasIdAndName)) {
            throw new IllegalArgumentException(getThesaurus().getFormat(MicroActionTranslationKeys.SET_CONNECTION_STATE_PROPERTY_MESSAGE).format());
        }
        usagePoint.setConnectionState(this.connectionStateFactory.unwrap((HasIdAndName) wrappedConnectionState));
    }
}
