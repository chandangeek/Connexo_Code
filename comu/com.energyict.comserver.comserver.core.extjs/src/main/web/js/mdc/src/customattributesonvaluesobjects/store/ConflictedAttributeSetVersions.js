Ext.define('Mdc.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.ConflictedAttributeSetVersionOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.ConflictedAttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'conflicts'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setChannelUrl: function (mRID, channelId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{mRID}/channels/{channelId}/customproperties/{customPropertySetId}/conflicts';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId).replace('{customPropertySetId}', customPropertySetId);
        },

        setChannelEditUrl: function (mRID, channelId, customPropertySetId, versionId) {
            var urlTpl = '/api/ddr/devices/{mRID}/channels/{channelId}/customproperties/{customPropertySetId}/conflicts/{versionId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{channelId}', channelId).replace('{customPropertySetId}', customPropertySetId).replace('{versionId}', versionId);
        },

        setRegisterUrl: function (mRID, registerId, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{mRID}/registers/{registerId}/customproperties/{customPropertySetId}/conflicts';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerId}', registerId).replace('{customPropertySetId}', customPropertySetId);
        },

        setRegisterEditUrl: function (mRID, registerId, customPropertySetId, versionId) {
            var urlTpl = '/api/ddr/devices/{mRID}/registers/{registerId}/customproperties/{customPropertySetId}/conflicts/{versionId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerId}', registerId).replace('{customPropertySetId}', customPropertySetId).replace('{versionId}', versionId);
        },

        setDeviceUrl: function (mRID, customPropertySetId) {
            var urlTpl = '/api/ddr/devices/{mRID}/customproperties/{customPropertySetId}/conflicts';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        },

        setDeviceEditUrl: function (mRID, customPropertySetId, versionId) {
            var urlTpl = '/api/ddr/devices/{mRID}/customproperties/{customPropertySetId}/conflicts/{versionId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId).replace('{versionId}', versionId);
        }
    }
});