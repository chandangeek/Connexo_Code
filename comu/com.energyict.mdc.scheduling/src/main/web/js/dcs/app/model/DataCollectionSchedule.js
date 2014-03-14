Ext.define('Dcs.model.DataCollectionSchedule', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'running',
        'name',
        'deviceGroupName',
        'schedule',
        'plannedDate'
    ],

    proxy: {
        type: 'rest',
        url: '/api/dcs/scheduling',
        headers: {"Accept": "application/json"},
        reader: {
            type: 'json',
            root: 'dataCollectionSchedules'
        }
    }


});
