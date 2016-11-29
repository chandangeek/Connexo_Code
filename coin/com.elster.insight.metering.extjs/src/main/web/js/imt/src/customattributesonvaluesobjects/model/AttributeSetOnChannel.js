Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetOnChannel', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/channels/{channelId}/customproperties'
    }
});
