Ext.define('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnChannel', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel',

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels/{channelId}/customproperties/{customPropertySetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        },

        setUrl: function (mRID, channelId, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});