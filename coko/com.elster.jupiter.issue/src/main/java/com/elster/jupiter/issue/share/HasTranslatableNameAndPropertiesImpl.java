package com.elster.jupiter.issue.share;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

public abstract class HasTranslatableNameAndPropertiesImpl implements HasTranslatableNameAndProperties, HasDynamicProperties {

    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService;
    
    //for OSGI
    public HasTranslatableNameAndPropertiesImpl() {
    }

    @Inject
    protected HasTranslatableNameAndPropertiesImpl(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDisplayName(String property) {
        return getThesaurus().getString(getPropertyNlsKey(property).getKey(), getPropertyDefaultFormat(property));
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getString(getNameNlsKey().getKey(), getNameDefaultFormat());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public NlsKey getNameNlsKey() {
        return SimpleNlsKey.key(IssueService.COMPONENT_NAME, Layer.DOMAIN, getBaseKey());
    }

    @Override
    public NlsKey getPropertyNlsKey(String property) {
        if (getPropertySpec(property) != null) {
            /*
             * Component=UNI and Layer=REST because the front-end will try to
             * translate the property itself, using unifyingjs framework
             */
            return SimpleNlsKey.key("UNI", Layer.REST, property);
        }
        return null;
    }

    protected final Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected final PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected String getBaseKey() {
        return this.getClass().getName();
    }
    
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
    
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }
}
