/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.readings.ReadingCorrection', {
    extend: 'Ext.data.Model',
    requires: [
    ],

    fields: [
        'type',
        'onlySuspectOrEstimated',
        'projected',
        'estimationComment',
        'amount',
        'intervals'
    ]
});
