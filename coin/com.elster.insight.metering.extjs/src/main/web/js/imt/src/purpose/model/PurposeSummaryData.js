/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.PurposeSummaryData', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        {name: 'interval', type: 'auto', useNull: true},
        {name: 'channelData', type: 'auto', useNull: true},
        {name: 'readingTime', type: 'auto', useNull: true},
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'value',
            persist: false,
            mapping: function(data){
                return _.values(data.channelData)[0]
            },
            type: 'auto'
        }
    ]
});