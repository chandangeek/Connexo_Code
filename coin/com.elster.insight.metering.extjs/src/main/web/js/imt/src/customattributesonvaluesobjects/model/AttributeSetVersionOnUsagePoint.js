Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnUsagePoint', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
    requires: [
        'Uni.util.Common'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties/{customPropertySetId}/versions',

        setUrl: function (mRID, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', Uni.util.Common.encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});