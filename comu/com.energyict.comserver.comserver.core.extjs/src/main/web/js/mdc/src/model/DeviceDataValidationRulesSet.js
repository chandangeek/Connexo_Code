/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceDataValidationRulesSet', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'isActive', type: 'boolean'},
        {name: 'numberOfInactiveRules', type: 'int'},
        {name: 'numberOfRules', type: 'int'},
        {name: 'numberOfVersions', type: 'int'},
        {name: 'hasCurrent', type: 'boolean'},
        {name: 'startDate'},
        {name: 'endDate'},
        {
            name: 'numberOfActiveRules',
            persist: false,
            mapping: function (data) {
                return data.numberOfRules - data.numberOfInactiveRules;
            }
        },
		{
            name: 'activeVersion',
            convert: function (value, record) {

                var result, startDate, endDate, numberOfVersions, hasCurrent;

                numberOfVersions = record.get('numberOfVersions');
                hasCurrent = record.get('hasCurrent');
                startDate = record.get('startDate');
                endDate = record.get('endDate');
                if (numberOfVersions === 0 || hasCurrent === false){
                    result = '-';
                } else if (startDate && endDate) {
                    result = Uni.I18n.translate('validationResults.version.fromxUntily', 'MDC', 'From {0} - Until {1}',
                        [Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                         Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.LONG, Uni.DateTime.SHORT)],
                        false
                    );
                } else if (startDate) {
                    result = Uni.I18n.translate('validationResults.version.fromx', 'MDC', 'From {0}',
                        Uni.DateTime.formatDateTime(new Date(startDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                        false
                    );
                } else if (endDate) {
                    result = Uni.I18n.translate('validationResults.version.untilx', 'MDC', 'Until {0}',
                        Uni.DateTime.formatDateTime(new Date(endDate), Uni.DateTime.LONG, Uni.DateTime.SHORT),
                        false
                    );
                }else {
                    result = Ext.String.format(Uni.I18n.translate('validationResults.version.notStart', 'MDC', 'Always'))
                }
				return result;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/validationrulesets',
        timeout: 60000,
        reader: {
            type: 'json',
            root: 'rulesets',
            totalProperty: 'total'
        }
    }
});