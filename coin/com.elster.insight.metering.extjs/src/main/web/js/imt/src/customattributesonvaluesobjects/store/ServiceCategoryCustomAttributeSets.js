Ext.define('Imt.customattributesonvaluesobjects.store.ServiceCategoryCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint'
    ],
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnUsagePoint',

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties/servicecategory',
        reader: {
            type: 'json',
            root: 'customPropertySets'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});