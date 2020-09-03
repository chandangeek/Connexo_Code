/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.data.push.enddeviceevents;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.upgrade.SqlExceptionThrowingFunction;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;

/**
 * Implements the {@link MessageHandlerFactory} for the {@link EndDeviceEventMessageHandler}.
 */
@Component(name = "com.energyict.mdc.data.push.enddeviceevents.EndDeviceEventMessageHandlerFactory",
        property = {"subscriber=" + EndDeviceEventMessageHandlerFactory.SUBSCRIBER_NAME, "destination=" + EventService.JUPITER_EVENTS},
        service = {MessageHandlerFactory.class, TranslationKeyProvider.class},
        immediate = true)
public class EndDeviceEventMessageHandlerFactory implements MessageHandlerFactory, TranslationKeyProvider {
    public static final String COMPONENT_NAME = "MDP";
    public static final String SUBSCRIBER_NAME = "PushEndDeviceEvents";
    public static final String SUBSCRIBER_DISPLAY_NAME = "Send web service notifications on device events";
    static final String DEVICE_GROUPS_PROPERTY_NAME = "cim.soap.deviceEventPush.deviceGroups";
    static final String CIM_EVENT_CODES_PROPERTY_NAME = "cim.soap.deviceEventPush.cimEventCodes";
    static final String DEVICE_EVENT_CODES_PROPERTY_NAME = "cim.soap.deviceEventPush.deviceEventCodes";
    static final String WS_ENDPOINTS_PROPERTY_NAME = "cim.soap.deviceEventPush.webServiceEndpoints";
    private static final Logger LOGGER = Logger.getLogger(EndDeviceEventMessageHandlerFactory.class.getName());
    private volatile JsonService jsonService;
    private volatile MeteringService meteringService;
    private volatile EndDeviceEventsServiceProvider endDeviceEventsServiceProvider;
    private volatile UpgradeService upgradeService;
    private volatile DataModel dataModel;
    private volatile MessageService messageService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile OrmService ormService;

    private volatile Condition deviceGroupNameCondition;
    private volatile Pattern cimEventCodePattern;
    private volatile Pattern deviceEventCodePattern;
    private volatile Set<String> endpointNames;

    public EndDeviceEventMessageHandlerFactory() {
        // for OSGi
    }

    // for tests
    @Inject
    public EndDeviceEventMessageHandlerFactory(BundleContext bundleContext,
                                               JsonService jsonService,
                                               MeteringService meteringService,
                                               EndDeviceEventsServiceProvider endDeviceEventsServiceProvider,
                                               UpgradeService upgradeService,
                                               EventService eventService,
                                               MessageService messageService,
                                               MeteringGroupsService meteringGroupsService,
                                               EndPointConfigurationService endPointConfigurationService,
                                               OrmService ormService) {
        setJsonService(jsonService);
        setMeteringService(meteringService);
        setEndDeviceEventsServiceProvider(endDeviceEventsServiceProvider);
        setUpgradeService(upgradeService);
        setEventService(eventService);
        setMessageService(messageService);
        setMeteringGroupsService(meteringGroupsService);
        setEndPointConfigurationService(endPointConfigurationService);
        setOrmService(ormService);
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(getModule());
        upgradeService.register(
                identifier("MultiSense", COMPONENT_NAME),
                dataModel,
                InstallerImpl.class,
                Collections.emptyMap());
        loadProperties(bundleContext);
    }

    private void loadProperties(BundleContext bundleContext) {
        deviceGroupNameCondition = getSetProperty(bundleContext, DEVICE_GROUPS_PROPERTY_NAME, Function.identity()).stream()
                .map(Where.where("name")::like)
                .reduce(Condition::or)
                .orElse(null);
        cimEventCodePattern = getPatternFromSetProperty(bundleContext, CIM_EVENT_CODES_PROPERTY_NAME);
        deviceEventCodePattern = getPatternFromSetProperty(bundleContext, DEVICE_EVENT_CODES_PROPERTY_NAME);
        endpointNames = getSetProperty(bundleContext, WS_ENDPOINTS_PROPERTY_NAME, Function.identity());
    }

    private static Pattern getPatternFromSetProperty(BundleContext bundleContext, String name) {
        String regex = getSetProperty(bundleContext, name, EndDeviceEventMessageHandlerFactory::transformToRegex).stream()
                .map(str -> "(?:" + str + ')')
                .collect(Collectors.joining("|"));
        return regex.isEmpty() ? null : Pattern.compile('^' + regex + '$');
    }

    private static String transformToRegex(String string) {
        return Pattern.quote(string).replace("?", "\\E.\\Q").replace("*", "\\E.*\\Q");
    }

    private static <T> Set<T> getSetProperty(BundleContext bundleContext, String name, Function<String, T> transformer) {
        Function<String, Set<T>> function = str -> Arrays.stream(str.split(";"))
                .map(String::trim)
                .filter(Predicates.not(String::isEmpty))
                .map(transformer)
                .collect(Collectors.toSet());
        return getProperty(bundleContext, name, function)
                .orElseGet(Collections::emptySet);
    }

    private static <T> Optional<T> getProperty(BundleContext bundleContext, String name, Function<String, T> transformer) {
        try {
            return Optional.ofNullable(bundleContext.getProperty(name))
                    .map(transformer);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Couldn't load property '" + name + "': " + e.getLocalizedMessage(), e);
            return Optional.empty();
        }
    }

    Set<EndPointConfiguration> getEndpoints() {
        return endpointNames.isEmpty() ?
                Collections.emptySet() :
                endPointConfigurationService.streamEndPointConfigurations()
                        .filter(Where.where("name").in(endpointNames))
                        .filter(Where.where("webServiceName").isEqualTo(EndDeviceEventsServiceProvider.NAME))
                        .collect(Collectors.toSet());
    }

    Optional<Pattern> getCimEventCodePattern() {
        return Optional.ofNullable(cimEventCodePattern);
    }

    Optional<Pattern> getDeviceEventCodePattern() {
        return Optional.ofNullable(deviceEventCodePattern);
    }

    boolean needToFilterByDeviceGroups() {
        return deviceGroupNameCondition != null;
    }

    Set<EndDeviceGroup> getDeviceGroups() {
        return deviceGroupNameCondition == null ?
                Collections.emptySet() :
                new HashSet<>(meteringGroupsService.getEndDeviceGroupQuery()
                        .select(deviceGroupNameCondition));
    }

    <T> T executeQuery(SqlBuilder queryBuilder, SqlExceptionThrowingFunction<ResultSet, T> mapper) {
        DataModel ormDataModel = ormService.getDataModel(MeteringGroupsService.COMPONENTNAME)
                .orElseThrow(() -> new IllegalStateException(DataModel.class.getSimpleName() + ' ' + MeteringGroupsService.COMPONENTNAME + " isn't found."));
        try (Connection connection = ormDataModel.getConnection(false);
             PreparedStatement statement = queryBuilder.prepare(connection);
             ResultSet resultSet = statement.executeQuery()) {
            return mapper.apply(resultSet);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public MessageHandler newMessageHandler() {
        return dataModel.getInstance(EndDeviceEventMessageHandler.class);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(DataPushTranslationKey.values());
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEndDeviceEventsServiceProvider(EndDeviceEventsServiceProvider endDeviceEventsServiceProvider) {
        this.endDeviceEventsServiceProvider = endDeviceEventsServiceProvider;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        // to make sure JupiterEvents queue already exists
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(JsonService.class).toInstance(jsonService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(EndDeviceEventsServiceProvider.class).toInstance(endDeviceEventsServiceProvider);
                bind(UpgradeService.class).toInstance(upgradeService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EndDeviceEventMessageHandlerFactory.class).toInstance(EndDeviceEventMessageHandlerFactory.this);
                bind(MessageService.class).toInstance(messageService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(OrmService.class).toInstance(ormService);
            }
        };
    }
}
