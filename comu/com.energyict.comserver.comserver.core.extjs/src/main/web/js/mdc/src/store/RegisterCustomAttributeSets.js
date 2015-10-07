Ext.define('Mdc.store.RegisterCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Cps.common.valuesobjects.model.AttributeSetOnObject'
    ],
    model: 'Cps.common.valuesobjects.model.AttributeSetOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/mds/devices/{mRID}/registers/{registerId}/custompropertysets',
        reader: {
            type: 'json',
            root: 'custompropertysets'
        },
        setUrl: function (mRID, channelId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerId}', channelId);
        }
    }
});