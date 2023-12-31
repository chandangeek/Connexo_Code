/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceConnectionMethod', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'status', type: 'string'},
        {name: 'direction', type: 'string', useNull: true},
        {name: 'displayDirection', type: 'string', useNull: true},
        {name: 'numberOfSimultaneousConnections', type: 'int'},
        {name: 'isDefault', type: 'boolean', useNull: true},
        {name: 'comPortPool', type: 'string', useNull: true},
        {name: 'connectionType', type: 'string', useNull: true},
        {name: 'connectionStrategyInfo', type: 'Mdc.model.field.ConnectionStrategy', useNull: true},
        'rescheduleRetryDelay',
        {name: 'nextExecutionSpecs', useNull: true, defaultValue: null},
        {name: 'comWindowStart',useNull: true, defaultValue: null},
        {name: 'comWindowEnd',useNull: true, defaultValue: null},
        {
            name: 'connectionWindow',
            persist: false,
            mapping: function (data) {
                return {start: data.comWindowStart, end: data.comWindowEnd};
            }
        },
        {name: 'protocolDialect', type: 'string', useNull: true},
        {name: 'protocolDialectDisplayName', type: 'string', useNull: true},
        {name: 'connectionFunctionInfo', type: 'Mdc.model.ConnectionFunction', useNull: true, defaultValue: null}

    ],
    associations: [
        {name: 'rescheduleRetryDelay',type: 'hasOne',model:'Mdc.model.field.TimeInfo',associationKey: 'rescheduleRetryDelay'},
        {name: 'properties', type: 'hasMany', model: 'Uni.property.model.Property', associationKey: 'properties', foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/connectionmethods'
    }
});