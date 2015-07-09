Ext.define('Fim.store.ImportServicesFilter', {
    extend: 'Ext.data.Store',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fir/importservices/list',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'importSchedules'
        },
        pageParam: false,
        startParam: false,
        limitParam: false

    },

    fields: [
        {name: 'id',       type: 'int'},
        {name: 'name',  type: 'string'}
    ]

});
