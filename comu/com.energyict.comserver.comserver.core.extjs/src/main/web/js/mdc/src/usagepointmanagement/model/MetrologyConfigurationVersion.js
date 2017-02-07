/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.model.MetrologyConfigurationVersion', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'editable', type: 'boolean'},
        {name: 'current', type: 'boolean'},
        {name: 'start', type: 'number', useNull: true},
        {name: 'end', type: 'number', useNull: true},
        {name: 'metrologyConfiguration', type: 'auto'},
        {
            name: 'period',
            type: 'string',
            persist: false,
            mapping: function (record) {
                var result = '';
                if (record.start) {
                    result += Uni.I18n.translate('general.history.from', 'MDC', 'From {0}', [Uni.DateTime.formatDateTimeShort(new Date(record.start))], false);

                    if(record.end){
                        result += Uni.I18n.translate('general.history.untill', 'MDC', ' until {0}', [Uni.DateTime.formatDateTimeShort(new Date(record.end))], false);
                    }
                }
                return result;
            }
        },
        {
            name: 'name',
            type: 'string',
            persist: false,
            mapping: function (record) {
                return record.metrologyConfiguration.name;
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/{usagePointId}/metrologyconfigurationversion',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});