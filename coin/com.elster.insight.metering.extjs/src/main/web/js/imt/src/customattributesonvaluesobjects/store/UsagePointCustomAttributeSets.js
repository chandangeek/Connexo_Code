Ext.define('Imt.customattributesonvaluesobjects.store.UsagePointCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/usagepoints/{mRID}/customproperties',
        reader: {
            type: 'json',
            root: 'customproperties'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});