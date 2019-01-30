/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.store.NonEstimatedDataStore', {
    extend: 'Ext.data.Store',
    fields: ['mRID', 'channelId', 'registerId', 'readingType', 'startTime', 'endTime', 'amountOfSuspects'],
    groupField : 'mRID' //'channelId'//'readingType'
});