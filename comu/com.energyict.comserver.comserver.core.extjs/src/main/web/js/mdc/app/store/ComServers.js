Ext.define('Mdc.store.ComServers',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComServer'
    ],
    autoLoad: true,
    model: 'Mdc.model.ComServer',
    storeId: 'ComServers',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers',
        reader: {
            type: 'json',
            root: 'comServers'
        }
    }
});
