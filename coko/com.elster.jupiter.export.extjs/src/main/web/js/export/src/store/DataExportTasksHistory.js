Ext.define('Dxp.store.DataExportTasksHistory', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.DataExportTaskHistory',
    autoLoad: false,

    //todo: replace dummy data
    data: [
        {
            id: 1,
            startedOn: '1415801034',
            finishedOn: '1415801052',
            duration: 12,
            status: 'sup',
            exportPeriodFrom: '1415801034',
            exportPeriodTo: '1415801034'
        },
        {
            id: 2,
            startedOn: '1415801034',
            finishedOn: '1415801052',
            duration: 12,
            status: 'sup',
            exportPeriodFrom: '1415801034',
            exportPeriodTo: '1415801034'
        }
    ]

    //proxy: {
    //    type: 'rest',
    //    url: '/api/export/dataexporttask',
    //    reader: {
    //        type: 'json',
    //        root: 'dataExportTasks'
    //    }
    //}
});
