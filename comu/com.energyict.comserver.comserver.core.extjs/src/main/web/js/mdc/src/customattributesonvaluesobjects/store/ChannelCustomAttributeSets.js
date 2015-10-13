Ext.define('Mdc.customattributesonvaluesobjects.store.ChannelCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/mds/devices/{mRID}/channels/{channelId}/custompropertysets',
        reader: {
            type: 'json',
            root: 'custompropertysets'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId);
        }
    }
});