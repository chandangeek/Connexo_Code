Ext.define('Idv.store.NonEstimatedDataStore', {
    extend: 'Ext.data.Store',
    fields: ['mRID', 'channelId','readingType', 'startTime', 'endTime', 'amountOfSuspects'],
    groupField : 'mRID' //'channelId'//'readingType'
});