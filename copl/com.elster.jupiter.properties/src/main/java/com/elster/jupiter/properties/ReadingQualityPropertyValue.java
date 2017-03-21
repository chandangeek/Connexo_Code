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
        this(cimCode);
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

        String[] theseCodes = getCimCode().split("\\.");
        String[] otherCodes = other.getCimCode().split("\\.");
        if (theseCodes.length != otherCodes.length) {
            return false;
        }
        String thisCode, otherCode;
        for (int i = 0; i < theseCodes.length; ++i) {
            thisCode = theseCodes[i];
            otherCode = otherCodes[i];
            if (!WILDCARD.equals(thisCode) && !WILDCARD.equals(otherCode) && !thisCode.equals(otherCode)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cimCode);
    }

    @Override
    public String toString() {
        return getCimCode();
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
