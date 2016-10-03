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
        beforeload: function (store, options) {
            var params = {};

            params.sort = Ext.JSON.encode([{property: 'name', direction: 'ASC'}]);

            Ext.apply(options.params, params);
        }
    }

});
