package com.elster.jupiter.properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/06/2016 - 17:43
 */
@XmlRootElement
public class ReadingQualityPropertyValue {

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

    @XmlAttribute
    public String getCimCode() {
        return cimCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReadingQualityPropertyValue that = (ReadingQualityPropertyValue) o;

        return getCimCode() != null ? getCimCode().equals(that.getCimCode()) : that.getCimCode() == null;

    }

    @Override
    public int hashCode() {
        return getCimCode() != null ? getCimCode().hashCode() : 0;
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