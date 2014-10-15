Ext.define('Mdc.store.Intervals', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.Interval',
    pageSize: undefined,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/mds/loadprofiles/intervals',
        reader: {
            type: 'json',
            root: 'data'
        }
    }

});
