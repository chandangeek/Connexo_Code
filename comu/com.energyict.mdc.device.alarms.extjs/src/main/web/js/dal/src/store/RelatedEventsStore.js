/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.RelatedEventsStore', {
    extend: 'Ext.data.Store',
    fields: [{name: 'eventDate', type: 'date', dateFormat: 'time'}, 'deviceType', 'deviceCode', 'domain', 'subDomain', 'eventOrAction', 'message']
});


