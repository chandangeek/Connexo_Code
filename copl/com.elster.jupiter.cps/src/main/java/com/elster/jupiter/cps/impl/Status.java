/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Show the status of the registered versus active {@link com.elster.jupiter.cps.CustomPropertySet}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-01 (13:25)
 */
class Status {

    private final List<RegisteredCustomPropertySetImpl> registeredCustomPropertySets;
    private final Collection<CustomPropertySet> publishedCustomPropertySets;
    private Formatter formatter;

    Status(List<RegisteredCustomPropertySetImpl> registeredCustomPropertySets, Collection<CustomPropertySet> publishedCustomPropertySets) {
        this.registeredCustomPropertySets = ImmutableList.copyOf(registeredCustomPropertySets);
        this.publishedCustomPropertySets = ImmutableList.copyOf(publishedCustomPropertySets);
    }

    void show() {
        this.formatter = new Formatter();
        this.formatter.printHeader();
        this.registeredCustomPropertySets
                .stream()
                .sorted(new RegisteredCustomPropertySetComparator())
                .forEach(this.formatter::print);
        this.formatter.printFooter();
        this.printPublishedButNotRegistered();
    }

    private void printPublishedButNotRegistered() {
        System.out.println("Published on whiteboard but not registered (most likely failed with " + UnderlyingSQLFailedException.class.getSimpleName() + ")");
        if (this.sortedPublishedCustomPropertySets().noneMatch(this::printIfNotRegistered)) {
            System.out.println("None");
        }
    }

    private Stream<CustomPropertySet> sortedPublishedCustomPropertySets() {
        return this.publishedCustomPropertySets.stream().sorted(new CustomPropertySetComparator());
    }

    private Boolean printIfNotRegistered(CustomPropertySet published) {
        Optional<String> registeredId = this.registeredCustomPropertySets
                .stream()
                .filter(RegisteredCustomPropertySetImpl::isActive)
                .map(RegisteredCustomPropertySetImpl::getCustomPropertySet)
                .map(CustomPropertySet::getId)
                .filter(id -> published.getId().equals(id))
                .findFirst();
        if (!registeredId.isPresent()) {
            this.formatter.printUnregistered(published);
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    private class Formatter {
        private static final String ID = "Registered Set ID";
        private static final String ACTIVE = "Active (Y/N)";
        private static final String TABLENAME = "Table name";

        private final int idColumnWidth;
        private final int tableNameColumnWidth;

        private Formatter() {
            this.idColumnWidth = registeredCustomPropertySets
                    .stream()
                    .filter(RegisteredCustomPropertySetImpl::isActive)
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .map(CustomPropertySet::getId)
                    .mapToInt(String::length)
                    .max()
                    .orElse(ID.length()) + 1;
            this.tableNameColumnWidth = registeredCustomPropertySets
                    .stream()
                    .filter(RegisteredCustomPropertySetImpl::isActive)
                    .map(RegisteredCustomPropertySet::getCustomPropertySet)
                    .map(CustomPropertySet::getPersistenceSupport)
                    .map(PersistenceSupport::tableName)
                    .mapToInt(String::length)
                    .max()
                    .orElse(TABLENAME.length()) + 1;
        }

        void printHeader() {
            System.out.println("* " + Strings.padEnd(ID + " " , this.idColumnWidth + 1, '*') + "* " + ACTIVE + " * " + Strings.padEnd(TABLENAME + " ", this.tableNameColumnWidth + 2, '*'));
        }

        void printFooter() {
            System.out.println(Strings.repeat("*" , this.idColumnWidth + 3) + Strings.repeat("*", ACTIVE.length() + 2) + Strings.repeat("*", this.tableNameColumnWidth + 5));
        }

        void print(RegisteredCustomPropertySet registeredCustomPropertySet) {
            System.out.println(this.toRow((RegisteredCustomPropertySetImpl) registeredCustomPropertySet));
        }

        private String toRow(RegisteredCustomPropertySetImpl registeredCustomPropertySet) {
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append("* ");
            String id;
            String tableName;
            String active;
            if (registeredCustomPropertySet.isActive()) {
                active = "Y";
                id = registeredCustomPropertySet.getCustomPropertySet().getId();
                tableName = registeredCustomPropertySet.getCustomPropertySet().getPersistenceSupport().tableName();
            } else {
                active = "N";
                id = registeredCustomPropertySet.getLogicalId();
                tableName = "Unknown";
            }
            rowBuilder.append(Strings.padEnd(id + " ", this.idColumnWidth, ' '));
            rowBuilder.append(" * ");
            rowBuilder.append(Strings.padEnd(active, ACTIVE.length(), ' '));
            rowBuilder.append(" * ");
            rowBuilder.append(Strings.padEnd(tableName, this.tableNameColumnWidth, ' '));
            rowBuilder.append(" *");
            return rowBuilder.toString();
        }

        void printUnregistered(CustomPropertySet customPropertySet) {
            System.out.println(this.toUnregisteredRow(customPropertySet));
        }

        private String toUnregisteredRow(CustomPropertySet customPropertySet) {
            return "* " +
                    Strings.padEnd(customPropertySet.getId() + " ", this.idColumnWidth, ' ') +
                    " * " +
                    Strings.padEnd("N", ACTIVE.length(), ' ') +
                    " * " +
                    Strings.padEnd(customPropertySet.getPersistenceSupport().tableName(), this.tableNameColumnWidth, ' ') +
                    " *";
        }
    }

    private class RegisteredCustomPropertySetComparator implements Comparator<RegisteredCustomPropertySetImpl> {
        @Override
        public int compare(RegisteredCustomPropertySetImpl o1, RegisteredCustomPropertySetImpl o2) {
            if (o1.isActive()) {
                if (o2.isActive()) {
                    return o1.getCustomPropertySet().getId().compareTo(o2.getCustomPropertySet().getId());
                } else {
                    return -1;
                }
            } else {
                if (o2.isActive()) {
                    return 1;
                } else {
                    return o1.getLogicalId().compareTo(o2.getLogicalId());
                }
            }
        }
    }

    private class CustomPropertySetComparator implements Comparator<CustomPropertySet> {
        @Override
        public int compare(CustomPropertySet o1, CustomPropertySet o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }

}