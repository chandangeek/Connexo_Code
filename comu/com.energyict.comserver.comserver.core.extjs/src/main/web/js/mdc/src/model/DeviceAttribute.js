Ext.define('Mdc.model.DeviceAttribute', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'mrid', type: 'auto'},
        {name: 'deviceType', type: 'auto'},
        {name: 'serialNumber', type: 'auto'},
        {name: 'deviceConfiguration', type: 'auto'},
        {name: 'yearOfCertification', type: 'auto'},
        {name: 'lifeCycleState', type: 'auto'},
        {name: 'batch', type: 'auto'},
        {name: 'usagePoint', type: 'auto'},
        {name: 'serviceCategory', type: 'auto'},
        {name: 'shipmentDate', type: 'auto'},
        {name: 'installationDate', type: 'auto'},
        {name: 'deactivationDate', type: 'auto'},
        {name: 'decommissioningDate', type: 'auto'},
        {name: 'deviceVersion', type: 'int'},
        {
            name: 'deviceConfigurationDisplay',
            persist: false,
            mapping: function (data) {
                var res = {
                    attributeId: data.deviceConfiguration.attributeId,
                    displayValue: data.deviceConfiguration.displayValue,
                    deviceTypeId: data.deviceType.attributeId,
                    available: data.deviceConfiguration.available,
                    editable: data.deviceConfiguration.editable
                };

                return res
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}',
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    }

});
