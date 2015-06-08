package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.InfoFactoryService;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Registers which InfoFactory can be used to convert which domain object. Used by the dynamicSearchResource
 */
@Component(name = "com.elster.jupiter.info.whiteboard.implementation" , immediate = true , service = {InfoFactoryService.class} )
public class InfoFactoryWhiteBoard implements InfoFactoryService {

    private final List<InfoFactory> factories = new CopyOnWriteArrayList<>();

    @Reference(name="Z-Order",cardinality= ReferenceCardinality.MULTIPLE,policy= ReferencePolicy.DYNAMIC)
    public void addFactory(InfoFactory infoFactory) {
        factories.add(infoFactory);
    }

    public void removeFactory(InfoFactory infoFactory) {
        factories.removeIf(fac -> infoFactory.getSearchDomainId().equals(fac.getSearchDomainId()));
    }


    @Override
    public InfoFactory getFactoryFor(String searchDomainId) {
        return factories.stream().
                filter(fac -> fac.getSearchDomainId().equals(searchDomainId)).
                findFirst().
                orElseThrow(() -> new IllegalStateException("No registered factory for " + searchDomainId));
    }
}
