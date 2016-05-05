Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
        'Uni.util.Common'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties/',

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', Uni.util.Common.encodeURIComponent(mRID));
        }
    }
});
