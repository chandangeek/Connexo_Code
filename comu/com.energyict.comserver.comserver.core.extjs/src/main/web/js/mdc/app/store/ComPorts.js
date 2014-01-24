Ext.define('Mdc.store.ComPorts',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ComPort'
    ],
    model: 'Mdc.model.ComPort',
    storeId: 'ComPorts',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/comports',
        reader: {
            type: 'json',
            root: 'ComPorts'
        }
    }
});
