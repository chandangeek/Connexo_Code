package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.InfoService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Created by bvn on 6/4/15.
 */

@Component(name = "com.elster.jupiter.info.whiteboard.implementation" , immediate = true , service = {InfoService.class}, property = "name=INFO" )
public class InfoFactoryWhiteBoard implements InfoService {

    private final List<InfoFactory<?>> factories = new ArrayList<>();

    @Reference(name="Z-Order",cardinality= ReferenceCardinality.MULTIPLE,policy= ReferencePolicy.DYNAMIC)
    public void addFactory(InfoFactory infoFactory, Map<String,Object> properties) {
        factories.add(infoFactory);
    }

    private String getDomainClass(Map<String, Object> properties) {
        return (String) Optional.ofNullable(properties.get("domainClass")).orElseThrow(() -> new IllegalStateException("Expected class property"));
    }

    public void removeFactory(InfoFactory infoFactory,Map<String,Object> properties) {
        factories.removeIf(fac -> infoFactory.getDomainClass().equals(fac.getDomainClass()));
    }


    @Override
    public Object from(Object domainObject) {
        InfoFactory infoFactory = factories.stream().
                filter(fac -> fac.getDomainClass().isAssignableFrom(domainObject.getClass())).
                findFirst().
                orElseThrow(() -> new IllegalStateException("No registered factory for " + domainObject.getClass()));
        return infoFactory.from(infoFactory.getDomainClass().cast(domainObject));
    }
}
