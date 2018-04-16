/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.PurposeSummaryData', {
    extend: 'Uni.model.Version',
    requires: [],
    fields: [
        {name: 'interval', type: 'auto', useNull: true},
        {name: 'channelData', type: 'auto'},
        {
            name: 'interval_end',
            persist: false,
            mapping: 'interval.end',
            dateFormat: 'time',
            type: 'date'
        }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Imt.purpose.model.Reading',
            associationKey: 'channelData',
            name: 'channelData'
        }
    ]
});