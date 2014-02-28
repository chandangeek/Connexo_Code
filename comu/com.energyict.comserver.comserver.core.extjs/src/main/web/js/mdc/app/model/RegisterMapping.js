Ext.define('Mdc.model.RegisterMapping', {
    extend: 'Mdc.model.Registertype',
    proxy: {
            type: 'rest',
            url: '../../api/dtc/devicetypes/{deviceType}/registertypes'
    }
});