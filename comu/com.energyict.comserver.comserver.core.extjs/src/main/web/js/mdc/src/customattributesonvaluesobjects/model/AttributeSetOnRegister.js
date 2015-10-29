Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetOnRegister', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/registers/{registerId}/customproperties',

        setUrl: function (mRID, registerId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerId}', registerId);
        }
    }
});
