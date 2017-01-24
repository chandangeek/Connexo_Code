Ext.define('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/customproperties/{customPropertySetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        }
    }
});