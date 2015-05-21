Ext.define('Fim.store.FileImporters', {
    extend: 'Ext.data.Store',
    model: 'Fim.model.FileImporter',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fir/importers',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'fileImporters'
        }
    },

    listeners: {
        beforeload: function (store, operation, eOpts) {
            store.getProxy().setExtraParam('application', typeof(MdcApp) != 'undefined' ? 'MDC' : typeof(SystemApp) != 'undefined' ? 'SYS' : null);
        }
    }
});
