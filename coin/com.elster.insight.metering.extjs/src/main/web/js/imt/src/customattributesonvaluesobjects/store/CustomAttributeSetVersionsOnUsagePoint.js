Ext.define('Imt.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnUsagePoint', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.util.Common'
    ],
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties/{customPropertySetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        },

        setUrl: function (mRID, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', Uni.util.Common.encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});