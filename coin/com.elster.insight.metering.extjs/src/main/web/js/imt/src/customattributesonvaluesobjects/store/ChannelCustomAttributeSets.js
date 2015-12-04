Ext.define('Imt.customattributesonvaluesobjects.store.ChannelCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels/{channelId}/customproperties',
        reader: {
            type: 'json',
            root: 'customproperties'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId);
        }
    }
});