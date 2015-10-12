Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetOnDevice', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/mds/devices/{mRID}/custompropertysets',

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});
