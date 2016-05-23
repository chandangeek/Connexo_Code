package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.TranslationKeys;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

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
    private String categoryName;

    @XmlAttribute
    private String indexName;

    public ReadingQualityInfo() {
    }

    public ReadingQualityInfo(String cimCode, String categoryName, String fullName) {
        this.cimCode = cimCode;
        this.categoryName = categoryName;
        this.indexName = fullName;
    }

    public static ReadingQualityInfo fromReadingQualityType(Thesaurus thesaurus, ReadingQualityType type) {
        ReadingQualityInfo result = new ReadingQualityInfo();

        result.setCimCode(type.getCode());

        if (type.category().isPresent()) {
            TranslationKeys translationKey = type.category().get().getTranslationKey();
            String translatedCategory = thesaurus.getStringBeyondComponent(translationKey.getKey(), translationKey.getDefaultFormat());
            result.setCategoryName(translatedCategory);
        } else {
            result.setCategoryName("");
        }

        if (type.qualityIndex().isPresent()) {
            TranslationKey translationKey = type.qualityIndex().get().getTranslationKey();
            String translatedIndex = thesaurus.getStringBeyondComponent(translationKey.getKey(), translationKey.getDefaultFormat());
            result.setIndexName(translatedIndex);
        } else {
            result.setIndexName("");
        }

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
}