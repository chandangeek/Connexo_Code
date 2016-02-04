Ext.define('Imt.customattributesonvaluesobjects.store.MetrologyConfigurationCustomAttributeSets', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnMetrologyConfiguration'
    ],
    model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnMetrologyConfiguration',

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/metrologyonfiguration/{id}/customproperties',
        reader: {
            type: 'json',
            root: 'customproperties'
        },
        pageParam: false,
        startParam: false,
        limitParam: false,
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(mRID));
        }
    }
});