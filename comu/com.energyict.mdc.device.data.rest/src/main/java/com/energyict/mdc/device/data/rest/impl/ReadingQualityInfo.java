package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 20/05/2016 - 14:58
 */
@XmlRootElement
public class ReadingQualityInfo {

    @XmlAttribute
    private String cimCode;

    @XmlAttribute
    private String systemName;

    @XmlAttribute
    private String categoryName;

    @XmlAttribute
    private String indexName;

    public ReadingQualityInfo() {
    }

    public static ReadingQualityInfo fromReadingQualityType(MeteringService meteringService, ReadingQualityType type) {
        ReadingQualityInfo result = new ReadingQualityInfo();
        result.setCimCode(type.getCode());
        result.setSystemName(type.system().map(meteringService::getDisplayName).orElse(""));
        result.setCategoryName(type.category().map(meteringService::getDisplayName).orElse(""));
        result.setIndexName(type.qualityIndex().map(meteringService::getDisplayName).orElse(""));
        return result;
    }

    public String getCimCode() {
        return cimCode;
    }

    public void setCimCode(String cimCode) {
        this.cimCode = cimCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
}
