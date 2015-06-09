package com.elster.jupiter.issue.share;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import aQute.bnd.annotation.ConsumerType;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasValidProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

@ConsumerType
@HasValidProperties(requiredPropertyMissingMessage = "{" + MessageSeeds.Keys.PROPERTY_MISSING + "}",
                    propertyNotInSpecMessage = "{" + MessageSeeds.Keys.PROPERTY_NOT_IN_PROPERTYSPECS + "}")
public abstract class AbstractIssueAction implements IssueAction {

    private final DataModel dataModel;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    
    protected Map<String, Object> properties = new HashMap<>();

    protected AbstractIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return issue != null;
    }

    @Override
    public IssueAction initAndValidate(Map<String, Object> properties) {
        this.properties = properties;
        Save.CREATE.validate(dataModel, this);
        return this;
    }
    
    @Override
    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream().filter(property -> property.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }
    
    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }
    
    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
