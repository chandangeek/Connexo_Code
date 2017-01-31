/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.PostalAddress;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.OrganizationBuilder;

import java.lang.reflect.Proxy;

public class OrganizationBuilderImpl implements OrganizationBuilder {

    private final OrganizationImpl organization;
    private OrganizationBuilder state = new NormalBuilder();

    public OrganizationBuilderImpl(DataModel dataModel, String mRID) {
        organization =  dataModel.getInstance(OrganizationImpl.class).init(mRID);
    }

    private class NormalBuilder implements OrganizationBuilder {
        @Override
        public OrganizationBuilder setMRID(String mRID) {
            organization.setMRID(mRID);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setName(String name) {
            organization.setName(name);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setAliasName(String aliasName) {
            organization.setAliasName(aliasName);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setDescription(String description) {
            organization.setDescription(description);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setElectronicAddress(ElectronicAddress electronicAddress) {
            organization.setElectronicAddress(electronicAddress);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setStreetAddress(StreetAddress streetAddress) {
            organization.setStreetAddress(streetAddress);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setPostalAddress(PostalAddress postalAddress) {
            organization.setPostalAddress(postalAddress);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setPhone1(TelephoneNumber telephoneNumber) {
            organization.setPhone1(telephoneNumber);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public OrganizationBuilder setPhone2(TelephoneNumber telephoneNumber) {
            organization.setPhone2(telephoneNumber);
            return OrganizationBuilderImpl.this;
        }

        @Override
        public Organization create() {
            state = usedState();
            organization.doSave();
            return organization;
        }

    }

    @Override
    public OrganizationBuilder setMRID(String mRID) {
        return state.setMRID(mRID);
    }

    @Override
    public OrganizationBuilder setName(String name) {
        return state.setName(name);
    }

    @Override
    public OrganizationBuilder setAliasName(String aliasName) {
        return state.setAliasName(aliasName);
    }

    @Override
    public OrganizationBuilder setDescription(String description) {
        return state.setDescription(description);
    }

    @Override
    public OrganizationBuilder setElectronicAddress(ElectronicAddress electronicAddress) {
        return state.setElectronicAddress(electronicAddress);
    }

    @Override
    public OrganizationBuilder setStreetAddress(StreetAddress streetAddress) {
        return state.setStreetAddress(streetAddress);
    }

    @Override
    public OrganizationBuilder setPostalAddress(PostalAddress postalAddress) {
        return state.setPostalAddress(postalAddress);
    }

    @Override
    public OrganizationBuilder setPhone1(TelephoneNumber telephoneNumber) {
        return state.setPhone1(telephoneNumber);
    }

    @Override
    public OrganizationBuilder setPhone2(TelephoneNumber telephoneNumber) {
        return state.setPhone2(telephoneNumber);
    }

    @Override
    public Organization create() {
        return state.create();
    }

    private OrganizationBuilder usedState() {
        return (OrganizationBuilder) Proxy.newProxyInstance(
                OrganizationBuilderImpl.class.getClassLoader(),
                new Class[]{OrganizationBuilder.class},
                (proxy, method, args) -> {
                    throw new IllegalStateException();
                });
    }
}
