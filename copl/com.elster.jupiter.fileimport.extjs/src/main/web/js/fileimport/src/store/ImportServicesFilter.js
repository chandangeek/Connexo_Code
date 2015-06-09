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
    ],
    listeners: {
        beforeload: function (store, operation, eOpts) {
            store.getProxy().setExtraParam('application', typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : null);

        }
    }

});
