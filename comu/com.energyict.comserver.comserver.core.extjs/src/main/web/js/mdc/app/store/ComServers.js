Ext.define('Mdc.store.ComServers',{
    requires: [
        'Mdc.model.ComServer',
    ],
    autoLoad: true,
    model: 'Mdc.model.ComServer',
    storeId: 'ComServers',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers',
        reader: {
            type: 'json',
            root: 'ComServers'
        }
    }
});
