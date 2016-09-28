Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/customproperties'
    }
});
