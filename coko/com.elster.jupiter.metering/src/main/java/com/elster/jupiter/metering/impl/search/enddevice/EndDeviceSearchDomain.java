package com.elster.jupiter.metering.impl.search.enddevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Condition;

/**
 * Provides an implementation for the {@link SearchDomain} interface
 * that supports {@link EndDevice}s.
 *
 */
@Component(name="com.elster.jupiter.metering.search.enddevice", service = SearchDomain.class, immediate = true)
public class EndDeviceSearchDomain implements SearchDomain {

    private volatile PropertySpecService propertySpecService;
    private volatile ServerMeteringService meteringService;
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public EndDeviceSearchDomain() {
        super();
    }

    // For Testing purposes
    @Inject
    public EndDeviceSearchDomain(PropertySpecService propertySpecService, ServerMeteringService meteringService, NlsService nlsService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
        this.setNlsService(nlsService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }


    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MessageService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public String getId() {
        return EndDevice.class.getName();
    }

    @Override
    public boolean supports(Class aClass) {
        return EndDevice.class.equals(aClass);
    }

    @Override
    public List<String> targetApplications() {
        return Arrays.asList("COKO", "COIN");
    }

    @Override
    public List<SearchableProperty> getProperties() {
        return new ArrayList<>(Arrays.asList(
                new MasterResourceIdentifierSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus()),
                new NameSearchableProperty(this, this.propertySpecService, this.meteringService.getThesaurus())
        ));
    }

    @Override
    public List<SearchableProperty> getPropertiesWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        if (!constrictions.isEmpty()) {
            throw new IllegalArgumentException("Expecting no constrictionsrtie");
        }
        else {
            return this.getProperties();
        }
    }

    @Override
    public List<SearchablePropertyValue> getPropertiesValues(Function<SearchableProperty, SearchablePropertyValue> mapper) {
        return getProperties()
                .stream()
                .map(mapper::apply)
                .filter(propertyValue -> propertyValue != null && propertyValue.getValueBean() != null && propertyValue.getValueBean().values != null)
                .collect(Collectors.toList());
    }

    @Override
    public Finder<?> finderFor(List<SearchablePropertyCondition> conditions) {
        return DefaultFinder.of(EndDevice.class, this.toCondition(conditions), this.meteringService.getDataModel())
                .defaultSortColumn("mRID");
    }

    @Override
    public String displayName() {
        return thesaurus.getFormat(PropertyTranslationKeys.ENDDEVICE_DOMAIN).format();
    }

    private Condition toCondition(List<SearchablePropertyCondition> conditions) {
        return conditions
                .stream()
                .map(ConditionBuilder::new)
                .reduce(
                    Condition.TRUE,
                    (underConstruction, builder) -> underConstruction.and(builder.build()),
                    Condition::and);
    }
    
    private class ConditionBuilder {
        private final SearchablePropertyCondition spec;
        private final SearchableEndDeviceProperty property;

        private ConditionBuilder(SearchablePropertyCondition spec) {
            super();
            this.spec = spec;
            this.property = (SearchableEndDeviceProperty) spec.getProperty();
        }

        private Condition build() {
            return this.property.toCondition(this.spec.getCondition());
        }

    }

}
