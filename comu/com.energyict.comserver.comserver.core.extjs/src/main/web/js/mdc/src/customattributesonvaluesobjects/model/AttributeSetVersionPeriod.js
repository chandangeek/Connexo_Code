Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionPeriod', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'start', dateFormat: 'time', type: 'date'},
        {name: 'end', dateFormat: 'time', type: 'date'}
    ],

    proxy: {
        type: 'rest',

        setDeviceUrl: function (mRID, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{mRID}/customproperties/{customPropertySetId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        },

        setChannelUrl: function (mRID, channelId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{mRID}/channels/{channelId}/customproperties/{customPropertySetId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId).replace('{customPropertySetId}', customPropertySetId);
        },

        setRegisterUrl: function (mRID, registerId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{mRID}/registers/{registerId}/customproperties/{customPropertySetId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerId}', registerId).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});
