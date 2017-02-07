/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.model.ValidationRuleSet', {
    extend: 'Uni.model.Version',
    fields: [
        'id', 'name',
        {name: 'currentVersionId', persist: false},
        {
            name: 'currentVersion',
            persist: false,
            mapping: function (data) {
                var result, startDate, endDate;

                if (!Ext.isString(data.currentVersion)) {
                    if ((data.numberOfVersions === 0) || (data.hasCurrent === false)) {
                        result = '-';
                    } else {
                        startDate = data.startDate;
                        endDate = data.endDate;
                        if (startDate && endDate) {
                            result = Uni.I18n.translate('validation.version.display.fromUntil', 'IMT', "From {0} - Until {1}",
                                [Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                                    Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.LONG, Uni.DateTime.SHORT)],
                                false
                            );
                        } else if (data.startDate) {
                            result = Uni.I18n.translate('validation.version.display.from', 'IMT', "From {0}",
                                Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                                false
                            );
                        } else if (data.endDate) {
                            result = Uni.I18n.translate('validation.version.display.until', 'IMT', "Until {0}",
                                Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                                false
                            );
                        } else {
                            result = Uni.I18n.translate('general.always', 'IMT', 'Always');
                        }
                    }
                } else {
                    result = data.currentVersion;
                }

                return result;
            }
        },
        {name: 'metrologyContract', persist: false},
        {name: 'metrologyContractIsMandatory', persist: false},
        {name: 'metrologyContractId', persist: false},
        {name: 'noRuleSets', persist: false, defaultValue: false},
        {
            name: 'uniqueId',
            persist: false,
            type: 'string'
        }
    ],
    idProperty: 'uniqueId'
});
