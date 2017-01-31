/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;
import com.energyict.mdc.protocol.pluggable.PluggableClassCreationException;
import com.energyict.mdc.protocol.pluggable.UnknownPluggableClassPropertiesException;

import javax.inject.Inject;
import java.net.URL;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wraps a {@link PluggableClass} while adding behavior that is
 * specific to protocol pluggable classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (11:02)
 */
public abstract class PluggableClassWrapper<T extends Pluggable> {

    private static final Pattern VERSION_DATE_PATTERN = Pattern.compile("\\$Date:(.*)\\$");

    private EventService eventService;
    private Thesaurus thesaurus;
    private PluggableClass pluggableClass;

    @Inject
    protected PluggableClassWrapper(EventService eventService, Thesaurus thesaurus) {
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    protected EventService getEventService() {
        return eventService;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

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

    public void setName(String name) {
        this.getPluggableClass().setName(name);
    }

    public PluggableClassType getPluggableClassType() {
        return this.getPluggableClass().getPluggableClassType();
    }

    public String getJavaClassName() {
        return this.getPluggableClass().getJavaClassName();
    }

    public Instant getModificationDate() {
        return null;
    }

    protected TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return this.getPluggableClass().getProperties(propertySpecs);
    }

    protected TypedProperties getProperties() {
        return this.getPluggableClass().getProperties(this.getPropertySpecs());
    }

    public void setProperty(PropertySpec propertySpec, Object value) {
        if (!isPropertySpec(propertySpec)) {
            throw new UnknownPluggableClassPropertiesException(this.thesaurus, this.asSet(propertySpec.getName()), this.getJavaClassName());
        }
        this.getPluggableClass().setProperty(propertySpec, value);
    }

    private Set<String> asSet(String propertySpecName) {
        Set<String> set = new HashSet<>();
        set.add(propertySpecName);
        return set;

    }

    private boolean isPropertySpec(PropertySpec propertySpec) {
        for (PropertySpec each : this.getPropertySpecs()) {
            if (each.getName().equals(propertySpec.getName())) {
                return true;
            }
        }
        return false;
    }

    public void removeProperty(PropertySpec propertySpec) {
        this.getPluggableClass().removeProperty(propertySpec);
    }

    public void save() {
        this.validate();
        this.getPluggableClass().save();
        long id = this.getPluggableClass().getId();
        if (id > 0) {
            this.eventService.postEvent(this.createEventType().topic(), this);
        }
        else {
            this.eventService.postEvent(this.updateEventType().topic(), this);
        }
    }

    public void delete() {
        this.getPluggableClass().delete();
        this.notifyDeleted();
    }

    protected abstract Discriminator discriminator();

    protected abstract void validateLicense ();

    protected void notifyDeleted() {
        this.eventService.postEvent(this.deleteEventType().topic(), this);
    }

    protected abstract CreateEventType createEventType();

    protected abstract UpdateEventType updateEventType();

    protected abstract DeleteEventType deleteEventType();

    protected abstract T newInstance (PluggableClass pluggableClass);

    protected void validate () {
        this.validateLicense();
        try {
            T pluggable = this.newInstance();
            this.discriminator().checkInterfaceCompatibility(pluggable, this.getThesaurus());
        }
        catch (UnableToCreateConnectionType e) {
            throw new PluggableClassCreationException(this.getThesaurus(), this.getJavaClassName(), e.getCause());
        }
    }

    protected String getVersion () {
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
                else if ("$Date$".equals(version)) {
                    return "Initial revision";
                }
                else {
                    Matcher matcher = VERSION_DATE_PATTERN.matcher(version);
                    if (matcher.matches()) {
                        return matcher.group(1).trim();
                    }
                    else {
                        return version;
                    }
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

    public long getEntityVersion() {
        return this.pluggableClass.getEntityVersion();
    }
}