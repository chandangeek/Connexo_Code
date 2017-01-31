/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement
public class ReadingQualityPropertyValue extends HasIdAndName {

    public static final String WILDCARD = "*";

    private String cimCode;
    private String systemName;
    private String categoryName;
    private String indexName;

    private ReadingQualityPropertyValue() {
        //JSon only
    }

    public ReadingQualityPropertyValue(String cimCode) {
        this.cimCode = cimCode;
    }

    public ReadingQualityPropertyValue(String cimCode, String systemName, String categoryName, String indexName) {
        this.cimCode = cimCode;
        this.systemName = systemName;
        this.categoryName = categoryName;
        this.indexName = indexName;
    }

    /**
     * ReadingQualityPropertyValues are equal if their CIM code is equal, taking wild cards into account.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ReadingQualityPropertyValue)) {
            return false;
        }
        ReadingQualityPropertyValue other = (ReadingQualityPropertyValue) obj;

        String thisCimCode = getCimCode();
        String thatCimCode = other.getCimCode();

        String test;
        String regex;

        if (thisCimCode.contains(WILDCARD)) {
            test = thatCimCode;
            regex = escape(thisCimCode).replaceAll("\\*", ".");     //Replace the * wildcards with the proper regex wildcard
        } else if (thatCimCode.contains(WILDCARD)) {
            test = thisCimCode;
            regex = escape(thatCimCode).replaceAll("\\*", ".");     //Replace the * wildcards with the proper regex wildcard
        } else {
            return thisCimCode.equals(thatCimCode);
        }

        return test.matches(regex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cimCode);
    }

    @Override
    public String toString() {
        return getCimCode();
    }

    /**
     * Escape the normal dot in the CIM code, so it is not interpreted as a regex character
     */
    private String escape(String cimCode) {
        return cimCode.replaceAll("\\.", "\\\\.");
    }

    @Override
    public String getId() {
        return getCimCode();
    }

    @Override
    public String getName() {
        return getCimCode();
    }

    @XmlAttribute
    public String getCimCode() {
        return cimCode;
    }

    @XmlAttribute
    public String getSystemName() {
        return systemName;
    }

    @XmlAttribute
    public String getCategoryName() {
        return categoryName;
    }

    @XmlAttribute
    public String getIndexName() {
        return indexName;
    }
}