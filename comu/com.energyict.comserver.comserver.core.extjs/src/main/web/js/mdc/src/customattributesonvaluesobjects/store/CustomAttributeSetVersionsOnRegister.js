Ext.define('Mdc.customattributesonvaluesobjects.store.CustomAttributeSetVersionsOnRegister', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],
    model: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/registers/{registerId}/customproperties/{customPropertySetId}/versions',
        reader: {
            type: 'json',
            root: 'versions'
        },

        setUrl: function (mRID, registerId, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerId}', registerId).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});