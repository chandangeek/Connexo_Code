Ext.define('Imt.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnUsagePoint', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.util.Common'
    ],
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/customproperties/{customPropertySetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        }
    }
});