Ext.define('Imt.customattributesonvaluesobjects.model.AttributeSetVersionPeriod', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'start', dateFormat: 'time', type: 'date'},
        {name: 'end', dateFormat: 'time', type: 'date'}
    ],

    proxy: {
        type: 'rest',

        setUsagePointUrl: function (mRID, customPropertySetId) {
            var urlTpl = '/api/udr/usagepoints/{mRID}/custompropertysets/{customPropertySetId}';

            this.url = urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{customPropertySetId}', customPropertySetId);
        }
    }
});