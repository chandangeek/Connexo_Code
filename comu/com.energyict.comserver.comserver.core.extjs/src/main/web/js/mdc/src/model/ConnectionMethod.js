/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ConnectionMethod', {
    extend: 'Uni.model.ParentVersion',
    requires: [
        'Uni.property.model.Property'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'direction', type: 'string', useNull: true},
        {name: 'displayDirection', type: 'string', useNull: true},
        {name: 'numberOfSimultaneousConnections', type: 'int'},
        {name: 'isDefault', type: 'boolean', useNull: true},
        {name: 'comPortPool', type: 'string', useNull: true},
        {name: 'connectionTypePluggableClass', type: 'string', useNull: true},
        {name: 'connectionStrategy', type: 'string', useNull: true},
        'rescheduleRetryDelay',
        {name: 'temporalExpression', useNull: true, defaultValue: null},
        {name: 'comWindowStart',type: 'int',useNull: true},
        {name: 'comWindowEnd',type: 'int',useNull: true},
        {
            name: 'connectionWindow',
            persist: false,
            mapping: function (data) {
                return {start: data.comWindowStart, end: data.comWindowEnd};
            }
        }
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
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods',
        reader: {
            type: 'json'
        }
    }

});