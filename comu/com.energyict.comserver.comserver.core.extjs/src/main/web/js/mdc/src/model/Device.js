Ext.define('Mdc.model.Device', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.DeviceLabel',
        'Mdc.model.DeviceConnection',
        'Mdc.model.DeviceCommunication',
        'Mdc.model.DataLoggerSlaveDevice'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'state', defaultValue: null},
        {name: 'serialNumber', type: 'string', useNull: true},
        {name: 'manufacturer', type: 'string', useNull: true},
        {name: 'modelNbr', type: 'string', useNull: true},
        {name: 'modelVersion', type: 'string', useNull: true},
        {name: 'deviceTypeId', type: 'number', useNull: true},
        {name: 'deviceTypeName', type: 'string', useNull: true},
        {name: 'deviceConfigurationId', type: 'number', useNull: true},
        {name: 'deviceConfigurationName', type: 'string', useNull: true},
        {name: 'shipmentDate', type: 'number'},
        {name: 'yearOfCertification', type: 'string', useNull: true},
        {name: 'batch', type: 'string', useNull: true},
        {name: 'masterDeviceName', defaultValue: null},
        {name: 'masterDeviceId', type: 'number', useNull: true},
        {name: 'nbrOfDataCollectionIssues', type: 'number', useNull: true},
        {name: 'openDataValidationIssue', type: 'number', useNull: true},
        {name: 'gatewayType', type: 'string', useNull: true},
        {name: 'creationTime', type: 'number', useNull: true},
        {name: 'linkingTimeStamp', type: 'number', useNull: true}, // Only for a data logger slave device
        {name: 'unlinkingTimeStamp', type: 'number', useNull: true}, // Only for a data logger slave device
        {name: 'dataloggerName', type: 'string', useNull: true}, // Only for a data logger slave device
        {name: 'isDirectlyAddressed', type: 'boolean'},
        {name: 'isGateway', type: 'boolean'},
        {name: 'isDataLogger', type: 'boolean'},
        {name: 'isDataLoggerSlave', type: 'boolean'},
        {name: 'hasLoadProfiles', type: 'boolean'},
        {name: 'hasLogBooks', type: 'boolean'},
        {name: 'hasRegisters', type: 'boolean'},
        {name: 'hasValidationRules', type: 'boolean'},
        {name: 'hasEstimationRules', type: 'boolean'},
        {name: 'usagePoint', type: 'string', useNull: true},
        {name: 'serviceCategory', type: 'string', useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'estimationStatus', defaultValue: null},
        {name: 'dataLoggerSlaveDevices', type: 'auto', defaultValue: null}
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
        },
        {
            name: 'dataLoggerSlaveDevices',
            type: 'hasMany',
            model: 'Mdc.model.DataLoggerSlaveDevice',
            associationKey: 'dataLoggerSlaveDevices',
            foreignKey: 'dataLoggerSlaveDevices'
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

