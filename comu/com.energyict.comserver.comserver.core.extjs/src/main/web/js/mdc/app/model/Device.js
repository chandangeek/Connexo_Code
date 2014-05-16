Ext.define('Mdc.model.Device', {
    extend: 'Ext.data.Model',
    requires: [

    ],
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'serialNumber', type: 'string', useNull: true},
        {name: 'deviceTypeId', type: 'number', useNull: true},
        {name: 'deviceTypeName', type: 'string', useNull: true},
        {name: 'deviceConfigurationId', type: 'number', useNull: true},
        {name: 'deviceConfigurationName', type: 'string', useNull: true},
        {name: 'yearOfCertification', type: 'string', useNull: true},
        {name: 'batch', type: 'string', useNull: true},
        {name: 'masterDevicemRID', type: 'string', useNull: true},
        {name: 'masterDeviceId', type: 'number', useNull: true},
        {name: 'nbrOfDataCollectionIssues', type: 'number', useNull: true}
    ],

    associations: [
            {name: 'slaveDevices', type: 'hasMany', model: 'Mdc.model.Device', associationKey: 'slaveDevices', foreignKey: 'slaveDevices',
                getTypeDiscriminator: function (node) {
                    return 'Mdc.model.Device';
                }
            }
        ],

    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices',
        reader: {
            type: 'json'
        }
    }

});

