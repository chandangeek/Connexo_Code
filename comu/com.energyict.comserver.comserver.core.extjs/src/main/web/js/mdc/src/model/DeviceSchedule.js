/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceSchedule', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name: 'id'},
        {
            name: 'internalId',
            convert: function(value,record){
                return record.get('id') + '-' + record.get('type');
            }
        },
        {name: 'masterScheduleId'},
        {name: 'name', type: 'string', useNull: true},
        {name: 'nextExecutionSpecs', useNull: true},
        {name: 'status', useNull: true},
        {name: 'schedule', useNull: true},
        {name: 'plannedDate', dateFormat: 'time', type: 'date'},
        {name: 'nextCommunication', dateFormat: 'time', type: 'date'},
        {name: 'comTask'},
        {name: 'active', type: 'boolean'},
        {name: 'hasConnectionWindow', type: 'boolean'},
        {name: 'connectionStrategyKey', type: 'string'},
        {name: 'connectionDefinedOnDevice', type: 'boolean'},
        {name: 'type', type: 'string'}
    ],
    idProperty: 'internalId',
    associations: [
        {name: 'comTaskInfos', type: 'hasMany', model: 'Mdc.model.ComTask', associationKey: 'comTaskInfos', foreignKey: 'comTaskInfos',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.ComTask';
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/schedules/',
        reader: {
            type: 'json'
        },
        setUrl: function (deviceMRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(deviceMRID));
        }
    }

});