Ext.define('Mdc.model.RegisterMapping', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'name', type: 'string'},
        {name:'obisCode', type: 'string'}
    ],
    proxy: {
            type: 'rest',
            url: '../../api/dtc/devicetypes/{deviceType}/registers'
    }
});