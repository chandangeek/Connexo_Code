/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Show the status of the registered versus active {@link com.elster.jupiter.cps.CustomPropertySet}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-01 (13:25)
 */
class Status {

    private final List<RegisteredCustomPropertySetImpl> registeredCustomPropertySets;
    private final Map<String, ActiveCustomPropertySet> activeCustomPropertySets;

    Status(List<RegisteredCustomPropertySetImpl> registeredCustomPropertySets, Map<String, ActiveCustomPropertySet> activeCustomPropertySets) {
        this.registeredCustomPropertySets = ImmutableList.copyOf(registeredCustomPropertySets);
        this.activeCustomPropertySets = ImmutableMap.copyOf(activeCustomPropertySets);
    }

    void show() {
        try {
            Header header = new Header();
            header.print();
            this.registeredCustomPropertySets
                    .stream()
                    .sorted(new RegisteredCustomPropertySetComparator())
                    .forEach(header::print);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private class Header {
        private static final String ID = "Registered Set ID";
        private static final String ACTIVE = "Active (Y/N)";
        private static final String TABLENAME = "Table name";

        private final int idColumnWidth;
        private final int tableNameColumnWidth;

        private Header() {
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

        void print() {
            System.out.println("* " + Strings.padEnd(ID + " " , this.idColumnWidth + 1, '*') + "* " + ACTIVE + " * " + Strings.padEnd(TABLENAME + " ", this.tableNameColumnWidth + 2, '*'));
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

}