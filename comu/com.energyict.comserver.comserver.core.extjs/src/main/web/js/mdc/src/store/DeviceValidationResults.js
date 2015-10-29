Ext.define('Mdc.store.DeviceValidationResults',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationResultsDataView',
    storeId: 'DeviceTypes',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/validationrulesets/validationmonitoring/dataview',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }

});
