/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.model.MetrologyConfigurationVersion', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'start', type: 'auto'},
        {name: 'metrologyConfiguration', type: 'auto'},
        {name: 'current', type: 'auto'},
        {name: 'purposesWithReadingTypes', type: 'auto'},
        {name: 'ongoingProcesses', type: 'auto'},
        {name: 'ongoingProcessesNumber', type: 'number'},
        {
            name: 'period',
            mapping: function (data) {
                var result = '-';
                if (data.start) {
                    result = 'From ' + Uni.DateTime.formatDateTimeShort(data.start);
                    if (data.end){
                        result += ' until ' + Uni.DateTime.formatDateTimeShort(data.end);
                    }
                }
                return result;
            }
        }
    ]
});

