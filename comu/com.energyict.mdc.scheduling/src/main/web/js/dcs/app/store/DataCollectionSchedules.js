Ext.define('Cfg.store.DataCollectionSchedules', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Cfg.model.DataCollectionSchedule',


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