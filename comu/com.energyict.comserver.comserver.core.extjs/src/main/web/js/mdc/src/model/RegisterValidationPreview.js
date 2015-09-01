Ext.define('Mdc.model.RegisterValidationPreview', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'validationActive',
            type: 'boolean'
        },
        {
            name: 'dataValidated',
            type: 'boolean'
        },
        {
            name: 'lastChecked',
            type: 'auto'
        }
    ],
    proxy: {
        type: 'rest',
        timeout: 120000,
        urlTpl: '/api/ddr/devices/{mRID}/registers/{registerID}/validationpreview',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID, registerId) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID)).replace('{registerID}', registerId);
        }
    }
});