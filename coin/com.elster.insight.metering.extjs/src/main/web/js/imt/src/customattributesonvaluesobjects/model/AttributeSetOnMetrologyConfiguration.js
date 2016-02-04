Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetOnMetrologyConfiguration', {
    extend: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/udr/metrologyonfiguration/{id}/customproperties',

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{id}', encodeURIComponent(mRID));
        }
    }
});
