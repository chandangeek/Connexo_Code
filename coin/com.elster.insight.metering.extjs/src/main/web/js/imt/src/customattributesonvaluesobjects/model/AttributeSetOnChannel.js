Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetOnChannel', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/channels/{channelId}/customproperties',

        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId);
        }
    }
});
