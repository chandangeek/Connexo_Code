Ext.define('Mdc.model.Device', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.DeviceLabel',
        'Mdc.model.DeviceConnection',
        'Mdc.model.DeviceCommunication'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'state', defaultValue: null},
        {name: 'serialNumber', type: 'string', useNull: true},
        {name: 'deviceTypeId', type: 'number', useNull: true},
        {name: 'deviceTypeName', type: 'string', useNull: true},
        {name: 'deviceConfigurationId', type: 'number', useNull: true},
        {name: 'deviceConfigurationName', type: 'string', useNull: true},
        {name: 'yearOfCertification', type: 'string', useNull: true},
        {name: 'batch', type: 'string', useNull: true},
        {name: 'masterDevicemRID', defaultValue: null},
        {name: 'masterDeviceId', type: 'number', useNull: true},
        {name: 'nbrOfDataCollectionIssues', type: 'number', useNull: true},
        {name: 'nbrOfDataValidationIssues', type: 'number', useNull: true},
        {name: 'gatewayType', type: 'string', useNull: true},
        {name: 'creationTime', dateFormat: 'time', type: 'date', useNull: true},
        {name: 'isDirectlyAddressed', type: 'boolean'},
        {name: 'isGateway', type: 'boolean'},
        {name: 'hasLoadProfiles', type: 'boolean'},
        {name: 'hasLogBooks', type: 'boolean'},
        {name: 'hasRegisters', type: 'boolean'},
        {name: 'usagePoint', type: 'string', useNull: true},
        {name: 'serviceCategory', type: 'string', useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'estimationStatus', defaultValue: null}
    ],

    associations: [
        {
            name: 'slaveDevices',
            type: 'hasMany',
            model: 'Mdc.model.Device',
            associationKey: 'slaveDevices',
            foreignKey: 'slaveDevices',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.Device';
            }
        },
        {
            name: 'labels',
            type: 'hasMany',
            model: 'Mdc.model.DeviceLabel',
            associationKey: 'labels',
            remoteFilter: true
        },
        {
            name: 'connections',
            type: 'hasMany',
            model: 'Mdc.model.DeviceConnection',
            associationKey: 'connections',
            remoteFilter: true,
            storeConfig: {
                pageSize: 5
            }
        },
        {
            name: 'communications',
            type: 'hasMany',
            model: 'Mdc.model.DeviceCommunication',
            associationKey: 'communications',
            remoteFilter: true,
            storeConfig: {
                pageSize: 5
            }
        }
    ],


    proxy: {
        type: 'rest',
        url: '/api/ddr/devices',
        reader: {
            type: 'json'
        }
    }

});

