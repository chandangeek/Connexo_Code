Ext.define('Mdc.model.DeviceProtocolDialect', {
    extend: 'Mdc.model.ProtocolDialect',
    fields: [
        {name: 'device', defaultValue: null},
        {name: 'version', defaultValue: undefined},
        {name: 'parent', defaultValue: undefined}
    ],
    proxy: {
        type: 'rest',
        url:  '../../api/ddr/devices/{mRID}/protocoldialects'
    }
});