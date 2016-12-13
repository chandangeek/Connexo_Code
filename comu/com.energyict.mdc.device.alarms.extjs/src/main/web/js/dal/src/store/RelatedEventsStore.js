Ext.define('Dal.store.RelatedEventsStore', {
    extend: 'Ext.data.Store',
    fields: [{name: 'eventDate', type: 'date', dateFormat: 'time'}, 'deviceType', 'deviceCode', 'domain', 'subDomain', 'eventOrAction', 'message']
});


