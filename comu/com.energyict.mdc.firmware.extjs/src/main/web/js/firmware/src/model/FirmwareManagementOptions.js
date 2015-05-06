Ext.define('Fwc.model.FirmwareManagementOptions', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'isAllowed',
            type: 'boolean',
            useNull: true
        },
        {
            name: 'supportedOptions',
            type: 'auto',
            useNull: true
        },
        {
            name: 'allowedOptions',
            type: 'auto',
            useNull: true
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/fwc/devicetypes/{deviceTypeId}/firmwaremanagementoptions',
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});