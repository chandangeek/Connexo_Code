package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig.UsagePointInfo.ServiceCategoryInfo;

import ch.iec.tc57._2011.usagepointconfig.Name;
import ch.iec.tc57._2011.usagepointconfig.ServiceCategory;
import ch.iec.tc57._2011.usagepointconfig.ServiceKind;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint;

import java.util.List;

public class Transformer {

    public UsagePointInfo transformFromXmlIntoJson(UsagePoint xml) {
        UsagePointInfo json = new UsagePointInfo();
        ServiceCategory serviceCategory = xml.getServiceCategory();
        if (serviceCategory != null) {
            ServiceCategoryInfo serviceCategoryInfo = new ServiceCategoryInfo();
            if (serviceCategory.getKind() != null) {
                serviceCategoryInfo.setServiceKind(serviceCategory.getKind().toString());
            }
            json.setServiceCategory(serviceCategoryInfo);
        }
        json.setName(retrieveName(xml.getNames()));
        return json;
    }

    public UsagePoint transformFromJsonIntoXml(UsagePointInfo json) {
        UsagePoint xml = new UsagePoint();
        ServiceCategoryInfo serviceCategoryInfo = json.getServiceCategory();
        if (serviceCategoryInfo != null) {
            ServiceCategory serviceCategory = new ServiceCategory();
            if (serviceCategoryInfo.getServiceKind() != null) {
                serviceCategory.setKind(ServiceKind.fromValue(serviceCategoryInfo.getServiceKind()));
            }
            xml.setServiceCategory(serviceCategory);
        }
        if (json.getName() != null) {
            Name name = new Name();
            name.setName(json.getName());
            xml.getNames().add(name);
        }
        return xml;
    }

    private String retrieveName(List<Name> names) {
        switch (names.size()) {
        case 0:
            return null;
        case 1:
            return names.get(0).getName();
        default:
            return names.stream().filter(name -> "UsagePointName".equals(name.getNameType().getName())).findFirst()
                    .map(Name::getName).orElse(null);
        }
    }
}
