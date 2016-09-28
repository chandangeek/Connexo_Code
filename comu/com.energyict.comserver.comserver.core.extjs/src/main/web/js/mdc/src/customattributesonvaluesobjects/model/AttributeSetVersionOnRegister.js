Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnRegister', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/registers/{registerId}/customproperties/{customPropertySetId}/versions'
    }
});
