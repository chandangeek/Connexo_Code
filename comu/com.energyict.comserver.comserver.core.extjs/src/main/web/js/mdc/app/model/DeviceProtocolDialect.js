Ext.define('Mdc.model.DeviceProtocolDialect', {
    extend: 'Mdc.model.ProtocolDialect',
    proxy: {
        type: 'rest',
        url:  '../../api/ddr/devices/{mRID}/protocoldialects'
    }
});