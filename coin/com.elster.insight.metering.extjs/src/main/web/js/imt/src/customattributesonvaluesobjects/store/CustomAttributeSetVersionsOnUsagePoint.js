Ext.define('Imt.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnUsagePoint', {
    extend: 'Ext.data.Store',
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties/{customPropertySetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        },

        setUrl: function (mRID, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});