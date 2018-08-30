/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.datavalidation.store.NonEstimatedDataStore', {
    extend: 'Ext.data.Store',
    fields: ['mRID', 'channelId', 'registerId', 'readingType', 'startTime', 'endTime', 'amountOfSuspects'],
    groupField: 'mRID'
});