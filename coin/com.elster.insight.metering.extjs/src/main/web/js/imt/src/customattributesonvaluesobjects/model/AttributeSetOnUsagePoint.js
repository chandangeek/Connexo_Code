Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties',
        url: '/api/udr/usagepoints/123/customproperties',

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});
