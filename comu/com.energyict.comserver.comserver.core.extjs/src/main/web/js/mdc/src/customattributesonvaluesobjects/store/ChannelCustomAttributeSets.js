Ext.define('Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',

    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/channels/{channelId}/customproperties',
        reader: {
            type: 'json',
            root: 'customproperties'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});