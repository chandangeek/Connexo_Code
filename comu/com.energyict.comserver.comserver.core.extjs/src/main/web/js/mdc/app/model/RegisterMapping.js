Ext.define('Mdc.model.RegisterMapping', {
    extend: 'Mdc.model.RegisterType',
    proxy: {
            type: 'rest',
            url: '../../api/dtc/devicetypes/{deviceType}/registertypes'
    }
});