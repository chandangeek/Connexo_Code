package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;

import javax.inject.Inject;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Wraps a {@link PluggableClass} while adding behavior that is
 * specific to protocol pluggable classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (11:02)
 */
public abstract class PluggableClassWrapper<T extends Pluggable> {

    private PluggableClass pluggableClass;
    @Inject
    private EventService eventService;

    protected PluggableClass getPluggableClass() {
        return pluggableClass;
    }

    protected void setPluggableClass(PluggableClass pluggableClass) {
        this.pluggableClass = pluggableClass;
    }

    public long getId() {
        return this.getPluggableClass().getId();
    }

    public String getName() {
        return this.getPluggableClass().getName();
    }

    public void setName(String name) throws BusinessException {
        this.getPluggableClass().setName(name);
    }

    public PluggableClassType getPluggableClassType() {
        return this.getPluggableClass().getPluggableClassType();
    }

    public String getJavaClassName() {
        return this.getPluggableClass().getJavaClassName();
    }

    public Date getModificationDate() {
        return null;
    }

    protected TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return this.getPluggableClass().getProperties(propertySpecs);
    }

    protected TypedProperties getProperties() {
        return this.getPluggableClass().getProperties(this.getPropertySpecs());
    }

    public void setProperty(PropertySpec propertySpec, Object value) {
        this.getPluggableClass().setProperty(propertySpec, value);
    }

    public void removeProperty(PropertySpec propertySpec) {
        this.getPluggableClass().removeProperty(propertySpec);
    }

    public void save() throws BusinessException, SQLException {
        this.validate();
        this.getPluggableClass().save();
    }

    public void delete() throws BusinessException, SQLException {
        this.notifyDelete();
        this.getPluggableClass().delete();
    }

    protected abstract Discriminator discriminator();

    protected abstract void validateLicense () throws BusinessException;

    protected void notifyDelete() {
        this.eventService.postEvent(EventType.DELETED.topic(), this);
    }

    protected abstract T newInstance (PluggableClass pluggableClass);

    protected void validate () throws BusinessException {
        this.validateLicense();
        try {
            T pluggable = this.newInstance();
            this.discriminator().checkInterfaceCompatibility(pluggable);
        }
        catch (UnableToCreateConnectionType e) {
            throw new BusinessException("PluggableClass.newInstance.failure", "Failure to create instance of pluggable class {0}", this.getJavaClassName(), e.getCause());
        }
    }

    public String getVersion () {
        try {
            T pluggable = this.newInstance();
            String version = pluggable.getVersion();
            if (version == null) {
                return null;
            }
            else {
                String prefix = "$Revision: ";
                if (version.startsWith(prefix)) {
                    return version.substring(prefix.length(), version.length() - 1);
                }
                else if (version.startsWith("$Date")) {
                    String[] date = version.split(" ");
                    if (date.length < 2) {
                        return version;
                    }
                    else {
                        return date[1];
                    }
                }
                else {
                    return version;
                }
            }
        }
        catch (UnableToCreateConnectionType e) {
            return null;
        }
    }

    protected T newInstance() {
        return this.newInstance(this.getPluggableClass());
    }

    public List<PropertySpec> getPropertySpecs () {
        return this.newInstance().getPropertySpecs();
    }

    public URL getDocumentationURL () {
        Class theClass = getClass();
        java.net.URL url;
        String base = this.getJavaClassName().replace('.', '/');
        String path = base + "_" + Locale.getDefault().toString();
        int index;
        boolean notFound;
        do {
            url = theClass.getResource("/" + path + ".html");
            notFound = (url == null);
            index = path.lastIndexOf('_');
            if (index != -1) {
                path = path.substring(0, index);
            }
        } while (notFound && (index != -1));
        return url;
    }

}