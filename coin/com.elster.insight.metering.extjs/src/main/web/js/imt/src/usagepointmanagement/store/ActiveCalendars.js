Ext.define('Imt.usagepointmanagement.store.ActiveCalendars', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.ActiveCalendar',
    //  fields: ['fromTime','toTime','next','calendar'],
    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{usagePointMRID}/calendars',
        reader: {
            type: 'json',
            root: 'calendars'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    },

    setMrid: function (mrid) {
        this.getProxy().url = this.getProxy()
            .urlTpl.replace('{usagePointMRID}', encodeURIComponent(mrid));
    }
});