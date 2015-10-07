Ext.define('Mdc.store.ChannelCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Cps.common.valuesobjects.model.AttributeSetOnObject'
    ],
    model: 'Cps.common.valuesobjects.model.AttributeSetOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/mds/devices/{mRID}/channels/{channelId}/custompropertysets',
        reader: {
            type: 'json',
            root: 'custompropertysets'
        },
        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId);
        }
    }
});