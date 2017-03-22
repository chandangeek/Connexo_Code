/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.model.DataQualityFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        { name: 'deviceGroup', type: 'auto' },
        { name: 'deviceType', type: 'auto' },
        { name: 'between', type: 'auto' },
        { name: 'readingQuality', type: 'auto' },
        { name: 'validator', type: 'auto' },
        { name: 'estimator', type: 'auto' },
        { name: 'amountOfSuspects', type: 'auto' },
        { name: 'amountOfConfirmed', type: 'auto' },
        { name: 'amountOfEstimates', type: 'auto' },
        { name: 'amountOfInformatives', type: 'auto' },
        { name: 'amountOfEdited', type: 'auto' }
    ]
});

