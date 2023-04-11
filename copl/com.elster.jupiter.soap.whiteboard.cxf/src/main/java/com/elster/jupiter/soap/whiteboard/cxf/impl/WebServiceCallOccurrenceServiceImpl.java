package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OccurrenceLogFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.PayloadSaveStrategy;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceFinderBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.elster.jupiter.soap.whiteboard.cxf.impl.MessageSeeds.WEB_SERVICE_CALL_OCCURRENCE_IS_ALREADY_IN_STATE;
import static com.elster.jupiter.util.conditions.Where.where;

public class WebServiceCallOccurrenceServiceImpl implements WebServiceCallOccurrenceService {
    private static final long OCCURRENCES_EXPIRE_AFTER_MINUTES = 30;

    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final NlsService nlsService;
    /* key for both maps is key of type */
    private final Map<String, TranslationKey> types = new ConcurrentHashMap<>();
    private final Map<String, LayerAndComponent> layerAndComponentsMap = new ConcurrentHashMap<>();
    private final Cache<Long, WebServiceCallOccurrence> occurrences;

    @Inject
    WebServiceCallOccurrenceServiceImpl(DataModel dataModel,
                                        TransactionService transactionService,
                                        Clock clock,
                                        Thesaurus thesaurus,
                                        NlsService nlsService) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.nlsService = nlsService;
        this.occurrences = CacheBuilder.newBuilder().expireAfterWrite(OCCURRENCES_EXPIRE_AFTER_MINUTES, TimeUnit.MINUTES).build();
    }

    @Override
    public WebServiceCallOccurrenceFinderBuilder getWebServiceCallOccurrenceFinderBuilder() {
        return new WebServiceCallOccurrenceFinderBuilderImpl(dataModel);
    }

    @Override
    public Optional<WebServiceCallOccurrence> getWebServiceCallOccurrence(long id) {
        return dataModel.mapper(WebServiceCallOccurrence.class)
                .getUnique("id", id);
    }

    @Override
    public Optional<WebServiceCallOccurrence> findAndLockWebServiceCallOccurrence(long id) {
        return Optional.ofNullable(dataModel.mapper(WebServiceCallOccurrence.class).lock(id));
    }

    @Override
    public Finder<WebServiceCallRelatedAttribute> getRelatedAttributesByValueLike(String value) {
        String dbSearchText = (value != null && !value.isEmpty()) ? "*" + value + "*" : "*";
        Condition typeCondition = where(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName()).likeIgnoreCase(dbSearchText);
        return DefaultFinder.of(WebServiceCallRelatedAttribute.class, typeCondition, this.dataModel).sorted(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName(), true);
    }

    @Override
    public Optional<WebServiceCallRelatedAttribute> getRelatedObjectById(long id) {
        return dataModel.mapper(WebServiceCallRelatedAttribute.class)
                .getOptional(id);
    }

    @Override
    public List<WebServiceCallRelatedAttribute> getRelatedAttributesByValue(String value) {
        return dataModel.mapper(WebServiceCallRelatedAttribute.class)
                .select(where(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName()).isEqualToIgnoreCase(value));
    }

    void addRelatedObjectTypes(String component, Layer layer, Map<String, TranslationKey> typesMap) {
        LayerAndComponent layerAndComponent = new LayerAndComponent(layer, component);
        types.putAll(typesMap);
        typesMap.keySet().forEach(key -> layerAndComponentsMap.put(key, layerAndComponent));
    }

    void removeRelatedObjectTypes(Map<String, TranslationKey> typesMap) {
        typesMap.keySet().forEach(key -> {
            types.remove(key);
            layerAndComponentsMap.remove(key);
        });
    }

    @Override
    public String translateAttributeType(String key) {
        LayerAndComponent layerAndComponent = layerAndComponentsMap.get(key);
        TranslationKey translationKey = types.get(key);
        Thesaurus thesaurus = nlsService.getThesaurus(layerAndComponent.getComponent(), layerAndComponent.getLayer());

        return thesaurus.getFormat(translationKey).format();
    }

    @Override
    public OccurrenceLogFinderBuilder getOccurrenceLogFinderBuilder() {
        return new OccurrenceLogFinderBuilderImpl(dataModel);
    }

    @Override
    public WebServiceCallOccurrence startOccurrence(EndPointConfiguration endPointConfiguration, String requestName, String application) {
        WebServiceCallOccurrence tmp = transactionService.executeInIndependentTransaction(
                () -> endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), requestName, application));
        occurrences.put(tmp.getId(), tmp);
        return tmp;
    }

    @Override
    public WebServiceCallOccurrence startOccurrence(EndPointConfiguration endPointConfiguration, String requestName, String application, String payload) {
        WebServiceCallOccurrence tmp = transactionService.executeInIndependentTransaction(
                () -> endPointConfiguration.createWebServiceCallOccurrence(clock.instant(), requestName, application, payload));
        occurrences.put(tmp.getId(), tmp);
        return tmp;
    }

    @Override
    public WebServiceCallOccurrence passOccurrence(long id) {
        occurrences.invalidate(id);
        return transactionService.executeInIndependentTransaction(() -> {
            WebServiceCallOccurrence tmp = lockOccurrenceOrThrowException(id);
            tmp.log(LogLevel.INFO, "Request completed successfully.");
            tmp.setEndTime(clock.instant());
            validateOngoingStatus(tmp);
            if (PayloadSaveStrategy.ALWAYS != tmp.getEndPointConfiguration().getPayloadSaveStrategy()) {
                tmp.setPayload(null);
            }
            tmp.setStatus(WebServiceCallOccurrenceStatus.SUCCESSFUL);
            tmp.save();
            return tmp;
        });
    }

    @Override
    public WebServiceCallOccurrence failOccurrence(long id, String message) {
        return failOccurrence(id, message, null);
    }

    @Override
    public WebServiceCallOccurrence failOccurrence(long id, Exception exception) {
        return failOccurrence(id, exception.getLocalizedMessage(), exception);
    }

    @Override
    public WebServiceCallOccurrence failOccurrence(long id, String message, Exception exception) {
        occurrences.invalidate(id);
        return transactionService.executeInIndependentTransaction(() -> {
            WebServiceCallOccurrence tmp = lockOccurrenceOrThrowException(id);
            if (exception == null) {
                tmp.log(LogLevel.SEVERE, message);
            } else {
                tmp.log(message, exception);
            }
            tmp.setEndTime(clock.instant());
            validateOngoingStatus(tmp);
            tmp.setStatus(WebServiceCallOccurrenceStatus.FAILED);
            tmp.save();
            return tmp;
        });
    }

    @Override
    public WebServiceCallOccurrence cancelOccurrence(long id) {
        occurrences.invalidate(id);
        return transactionService.executeInIndependentTransaction(() -> {
            WebServiceCallOccurrence tmp = lockOccurrenceOrThrowException(id);
            tmp.log(LogLevel.INFO, "Request has been cancelled.");
            tmp.setEndTime(clock.instant());
            validateOngoingStatus(tmp);
            tmp.setStatus(WebServiceCallOccurrenceStatus.CANCELLED);
            tmp.save();
            return tmp;
        });
    }

    private void validateOngoingStatus(WebServiceCallOccurrence occurrence) {
        WebServiceCallOccurrenceStatus status = occurrence.getStatus();
        if (status != WebServiceCallOccurrenceStatus.ONGOING) {
            throw new IllegalWebServiceCallOccurrenceStateException(thesaurus, WEB_SERVICE_CALL_OCCURRENCE_IS_ALREADY_IN_STATE, status.translate(thesaurus));
        }
    }

    private WebServiceCallOccurrence lockOccurrenceOrThrowException(long id) {
        return findAndLockWebServiceCallOccurrence(id)
                .orElseThrow(() -> new IllegalStateException("Web service call occurrence isn't present."));
    }

    @Override
    public WebServiceCallOccurrence getOngoingOccurrence(long id) {
        WebServiceCallOccurrence tmp = occurrences.getIfPresent(id);
        if (tmp == null) {
            tmp = getWebServiceCallOccurrence(id)
                    .orElseThrow(() -> new IllegalStateException("Web service call occurrence isn't present."));
        }
        return tmp;
    }

    private static class LayerAndComponent {
        private final Layer layer;
        private final String component;

        private LayerAndComponent(Layer layer, String component) {
            this.layer = layer;
            this.component = component;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                    || obj instanceof LayerAndComponent
                    && Objects.equals(layer, ((LayerAndComponent) obj).layer)
                    && Objects.equals(component, ((LayerAndComponent) obj).component);
        }

        @Override
        public int hashCode() {
            return Objects.hash(layer, component);
        }

        public Layer getLayer() {
            return this.layer;
        }

        public String getComponent() {
            return this.component;
        }

        @Override
        public String toString() {
            return "Layer=" + layer + " ,component = " + component;
        }
    }
}
