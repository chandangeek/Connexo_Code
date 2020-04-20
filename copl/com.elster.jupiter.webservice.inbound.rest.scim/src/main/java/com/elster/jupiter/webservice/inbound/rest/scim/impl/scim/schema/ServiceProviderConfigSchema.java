package com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.AuthenticationScheme;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.BasicConfigurationSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.BulkConfigurationSchema;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.scim.schema.configuration.FilterConfigurationSchema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceProviderConfigSchema extends BaseSchema {

    private BasicConfigurationSchema patch;

    private BulkConfigurationSchema bulk;

    private FilterConfigurationSchema filter;

    private BasicConfigurationSchema changePassword;

    private BasicConfigurationSchema sort;

    private BasicConfigurationSchema etag;

    private AuthenticationScheme[] authenticationSchemes;

    public BasicConfigurationSchema getPatch() {
        return patch;
    }

    public void setPatch(BasicConfigurationSchema patch) {
        this.patch = patch;
    }

    public BulkConfigurationSchema getBulk() {
        return bulk;
    }

    public void setBulk(BulkConfigurationSchema bulk) {
        this.bulk = bulk;
    }

    public FilterConfigurationSchema getFilter() {
        return filter;
    }

    public void setFilter(FilterConfigurationSchema filter) {
        this.filter = filter;
    }

    public BasicConfigurationSchema getChangePassword() {
        return changePassword;
    }

    public void setChangePassword(BasicConfigurationSchema changePassword) {
        this.changePassword = changePassword;
    }

    public BasicConfigurationSchema getSort() {
        return sort;
    }

    public void setSort(BasicConfigurationSchema sort) {
        this.sort = sort;
    }

    public BasicConfigurationSchema getEtag() {
        return etag;
    }

    public void setEtag(BasicConfigurationSchema etag) {
        this.etag = etag;
    }

    public AuthenticationScheme[] getAuthenticationSchemes() {
        return authenticationSchemes;
    }

    public void setAuthenticationSchemes(AuthenticationScheme[] authenticationSchemes) {
        this.authenticationSchemes = authenticationSchemes;
    }
}
