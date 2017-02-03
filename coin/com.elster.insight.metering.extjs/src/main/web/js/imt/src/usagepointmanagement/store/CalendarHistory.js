/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.CalendarHistory', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.ActiveCalendar',
    //  fields: ['fromTime','toTime','next','calendar'],
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointName}/history/calendars',
        reader: {
            type: 'json',
            root: 'calendars'
        }
    },

    setName: function (name) {
        this.getProxy().url = this.getProxy()
            .urlTpl.replace('{usagePointName}', encodeURIComponent(name));
    }
});