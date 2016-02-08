Ext.define('Imt.customattributesonvaluesobjects.store.ConflictedAttributeSetVersions', {
    extend: 'Ext.data.Store',
    model: 'Imt.customattributesonvaluesobjects.model.ConflictedAttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        reader: {
            type: 'json',
            root: 'conflicts'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,

        setUsagePointUrl: function (mRID, customPropertySetId) {
            var urlTpl = '/api/udr/usagepoints/{mRID}/custompropertysets/{customPropertySetId}/conflicts';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        },

        setUsagePointEditUrl: function (mRID, customPropertySetId, versionId) {
            var urlTpl = '/api/udr/usagepoints/{mRID}/custompropertysets/{customPropertySetId}/conflicts/{versionId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId).replace('{versionId}', versionId);
        }
    }
});