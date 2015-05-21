Ext.define('Fim.store.ImportServices', {
    extend: 'Ext.data.Store',
    model: 'Fim.model.ImportService',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fir/importservices',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'importSchedules'
        }
    },
    listeners: {
        beforeload: function (store, operation, eOpts) {
            store.getProxy().setExtraParam('application', typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : null);
        }
    }

});
