Ext.define('Dxp.store.DataExportTaskFilter', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/export/dataexporttask',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'dataExportTasks'
        },
        pageParam: false,
        startParam: false,
        limitParam: false

    },

    fields: [
        {name: 'id', type: 'int'},
        {name: 'name',  type: 'string'}
    ]

});
