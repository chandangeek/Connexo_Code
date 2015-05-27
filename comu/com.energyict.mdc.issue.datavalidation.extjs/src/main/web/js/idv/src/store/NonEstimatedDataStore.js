Ext.define('Idv.store.NonEstimatedDataStore', {
    extend: 'Ext.data.Store',
    fields: ['channelId','readingType', 'startTime', 'endTime', 'amountOfSuspects'],
    groupField : 'channelId' //'channelId'//'readingType'
});