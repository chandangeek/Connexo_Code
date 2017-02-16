/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DataLoggerSlaveDevice', {
    requires: [
        'Mdc.model.DataLoggerSlaveChannel'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string', useNull: true},
        {name: 'serialNumber', type: 'string', useNull: true},
        {name: 'deviceTypeId', type: 'number', useNull: true},
        {name: 'deviceTypeName', type: 'string', useNull: true},
        {name: 'deviceConfigurationId', type: 'number', useNull: true},
        {name: 'deviceConfigurationName', type: 'string', useNull: true},
        {name: 'yearOfCertification', type: 'string', useNull: true},
        {name: 'batch', type: 'string', useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'shipmentDate',type: 'number', useNull: true},
        {name: 'linkingTimeStamp',type: 'number', useNull: true},
        {name: 'unlinkingTimeStamp',type: 'number', useNull: true},
        {name: 'dataLoggerSlaveChannelInfos', type: 'auto', defaultValue: null}
    ],

    associations: [
        {
            name: 'dataLoggerSlaveChannelInfos',
            type: 'hasMany',
            model: 'Mdc.model.DataLoggerSlaveChannel',
            associationKey: 'dataLoggerSlaveChannelInfos',
            foreignKey: 'dataLoggerSlaveChannelInfos'
        }
    ]

});