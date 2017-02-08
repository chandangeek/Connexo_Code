/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.util.ServiceCategoryTranslations', {
    singleton: true,

    translations: {
        ELECTRICITY: Uni.I18n.translate('general.attributes.usagePoint.electricity', 'IMT', 'Electricity'),
        GAS: Uni.I18n.translate('general.attributes.usagePoint.gas', 'IMT', 'Gas'),
        WATER: Uni.I18n.translate('general.attributes.usagePoint.water', 'IMT', 'Water'),
        HEAT: Uni.I18n.translate('general.attributes.usagePoint.heat', 'IMT', 'Heat')
    },

    getTranslation: function (key) {
        return this.translations[key];
    }
});