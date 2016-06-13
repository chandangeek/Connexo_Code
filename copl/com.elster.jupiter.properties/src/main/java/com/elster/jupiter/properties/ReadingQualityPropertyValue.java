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
public class ReadingQualityPropertyValue extends HasIdAndName {

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