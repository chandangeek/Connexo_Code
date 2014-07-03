Ext.define('Mdc.store.ComServerComPorts',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ComServerComPort',
    pageSize: 10,
    sorters: [{
        property: 'name',
        direction: 'ASC'
    }],
    sortOnLoad: true,
    proxy: {
        type: 'rest',
        url: '../../api/mdc/comservers/{comServerId}/comports',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
