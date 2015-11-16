Ext.define('Mdc.store.StaticGroupDevices', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.search.Result',
    remoteFilter: true,
    pageSize: 200,
    buffered: true,

    proxy: {
        timeout: 9999999,
        type: 'ajax',
        reader: {
            type: 'json',
            root: 'searchResults'
        }
    }
});