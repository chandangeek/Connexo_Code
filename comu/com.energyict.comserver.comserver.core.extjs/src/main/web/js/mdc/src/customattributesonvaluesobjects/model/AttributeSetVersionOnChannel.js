Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnChannel', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels/{channelId}/customproperties/{customPropertySetId}/versions',

        setUrl: function (mRID, channelId, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});
