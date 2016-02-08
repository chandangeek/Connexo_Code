Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnUsagePoint', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/custompropertysets/{customPropertySetId}/versions',

        setUrl: function (mRID, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});