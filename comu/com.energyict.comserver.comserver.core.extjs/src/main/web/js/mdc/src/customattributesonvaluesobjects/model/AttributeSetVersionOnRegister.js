Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnRegister', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/registers/{registerId}/customproperties/{customPropertySetId}/versions',

        setUrl: function (mRID, registerId, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerId}', registerId).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});
