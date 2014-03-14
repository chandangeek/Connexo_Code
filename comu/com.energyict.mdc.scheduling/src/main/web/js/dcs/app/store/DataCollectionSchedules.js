Ext.define('Dcs.store.DataCollectionSchedules', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Dcs.model.DataCollectionSchedule',


    proxy: {
        type: 'rest',
        url: '/api/val/validation',
        reader: {
            type: 'json',
            root: 'dataCollectionSchedules',
            totalProperty: 'total'
        }
    }


});