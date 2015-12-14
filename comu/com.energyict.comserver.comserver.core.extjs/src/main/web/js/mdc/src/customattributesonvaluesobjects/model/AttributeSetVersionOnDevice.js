Ext.define('Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnDevice', {
    extend: 'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject',
    requires: [
        'Mdc.customattributesonvaluesobjects.model.AttributeSetVersionOnObject'
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/customproperties/{customPropertySetId}/versions',

        setUrl: function (mRID, customPropertySetId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});
