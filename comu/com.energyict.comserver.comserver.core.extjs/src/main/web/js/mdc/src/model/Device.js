/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.Device', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Mdc.model.DeviceLabel',
        'Mdc.model.DeviceConnection',
        'Mdc.model.DeviceCommunication',
        'Mdc.model.DataLoggerSlaveDevice',
        'Mdc.model.G3NodePLCInfo',
        'Mdc.model.DeviceZones'
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
        {name: 'multiElementDeviceName', type: 'string', useNull: true}, // Only for a data logger slave device
        {name: 'isDirectlyAddressed', type: 'boolean'},
        {name: 'isGateway', type: 'boolean'},
        {name: 'isDataLogger', type: 'boolean'},
        {name: 'isDataLoggerSlave', type: 'boolean'},
        {name: 'isMultiElementDevice', type: 'boolean'},
        {name: 'isMultiElementSlave', type: 'boolean'},
        {name: 'hasLoadProfiles', type: 'boolean'},
        {name: 'hasLogBooks', type: 'boolean'},
        {name: 'hasRegisters', type: 'boolean'},
        {name: 'hasValidationRules', type: 'boolean'},
        {name: 'isFirmwareManagementAllowed', type: 'boolean'},
        {name: 'hasEstimationRules', type: 'boolean'},
        {name: 'usagePoint', type: 'string', useNull: true},
        {name: 'serviceCategory', type: 'string', useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'estimationStatus', defaultValue: null},
        {name: 'dataLoggerSlaveDevices', type: 'auto', defaultValue: null},
        {name: 'protocolNeedsImageIdentifierForFirmwareUpgrade', type: 'boolean'},
        {name: 'g3NodePLCInfo', type: 'auto', defaultValue: null, persist: false},
        {name: 'zones', defaultValue: null},
        {name: 'zoneTypeName', type: 'string'},
        {name: 'zoneTypeId', type: 'int'},
        {name: 'zoneName', type: 'string'},
        {name: 'zoneId', type: 'int'},
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
            name: 'zones',
            type: 'hasMany',
            model: 'Mdc.model.Device',
            associationKey: 'zones',
            foreignKey: 'zones',
            remoteFilter: true,
            storeConfig: {
                pageSize: 5
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
        },
        {
            name: 'g3NodePLCInfo',
            type: 'hasOne',
            model: 'Mdc.model.G3NodePLCInfo',
            associationKey: 'g3NodePLCInfo',
            getterName: 'getG3NodePLCInfo',
            setterName: 'setG3NodePLCInfo',
            foreignKey: 'g3NodePLCInfo'
        }
    ],


    proxy: {
        type: 'rest',
        url: '/api/ddr/devices',
        timeout: 60000,
        reader: {
            type: 'json'
        }
    }
});

