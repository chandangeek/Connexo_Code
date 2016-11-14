Ext.define('Imt.usagepointmanagement.store.CalendarHistory', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.ActiveCalendar',
    //  fields: ['fromTime','toTime','next','calendar'],
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointMRID}/history/calendars',
        reader: {
            type: 'json',
            root: 'calendars'
        }
    },

    setMrid: function (mrid) {
        this.getProxy().url = this.getProxy()
            .urlTpl.replace('{usagePointMRID}', encodeURIComponent(mrid));
    }
});