package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetSearchEnabler;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.search.SearchablePropertyConstriction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.metering.impl.search.UsagePointCustomPropertySetEnabler", service = CustomPropertySetSearchEnabler.class, immediate = true)
public class UsagePointCustomPropertySetEnabler implements CustomPropertySetSearchEnabler {
    private MeteringService meteringService;

    @Override
    public Class getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public boolean enableWhen(CustomPropertySet customPropertySet, List<SearchablePropertyConstriction> constrictions) {
        if (constrictions.isEmpty()) {
            return false;
        }
        boolean result = false;
        for (SearchablePropertyConstriction constriction : constrictions) {
            if (constriction.getConstrainingProperty().hasName(ServiceCategorySearchableProperty.FIELDNAME)) {
                result = constriction.getConstrainingValues()
                        .stream()
                        .map(ServiceKind.class::cast)
                        .map(kind -> this.meteringService.getServiceCategory(kind))
                        .filter(Optional::isPresent)
                        .flatMap(sc -> sc.get().getCustomPropertySets().stream())
                        .map(RegisteredCustomPropertySet::getCustomPropertySet)
                        .map(CustomPropertySet::getId)
                        .anyMatch(id -> id.equals(customPropertySet.getId()));
            }
            // TODO add check for metrology configuration!
            if (result) {
                break;
            }
        }
        return result;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
}
