Ext.define('Mdc.store.DeviceConfigurationResults',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationResults',
    storeId: 'DeviceTypes',
    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/validationrulesets/validationmonitoring/configurationview',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }

});
