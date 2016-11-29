Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnUsagePoint', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
    requires: [
        'Uni.util.Common'
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/customproperties/{customPropertySetId}/versions'
    }
});