/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceType', {
    extend: 'Uni.model.Version',
    requires: [
        'Mdc.model.DeviceProtocol',
        'Mdc.model.field.TimePeriod'
    ],
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'loadProfileCount', type: 'int', useNull: true},
        {name: 'deviceIcon', type: 'string', useNull: true},
        {name: 'registerCount', type: 'int', useNull: true},
        {name: 'logBookCount', type: 'int', useNull: true},
        {name: 'deviceConfigurationCount', type: 'int', useNull: true},
        {name: 'activeDeviceConfigurationCount', type: 'int', useNull: true},
        {name: 'deviceConflictsCount', type: 'int', useNull: true},
        {name: 'canBeGateway', type: 'boolean', useNull: true},
        {name: 'canBeDirectlyAddressed', type: 'boolean', useNull: true},
        {name: 'deviceProtocolPluggableClass', type: 'string', useNull: true},
        {name: 'deviceProtocolPluggableClassId', type: 'number', useNull: true},
        {name: 'registerTypes'},
        {name: 'deviceLifeCycleId'},
        {name: 'deviceLifeCycleName'},
        {name: 'deviceTypePurpose'},
        {name: 'version', type: 'number', useNull: true},
        {name: 'fileManagementEnabled', type: 'boolean'},
        {name: 'isLogicalSlave', type: 'boolean', useNull: true},
        {name: 'needsImageIdentifierForFirmware', type: 'boolean', useNull: true}
    ],
    associations: [
            {name: 'registerTypes', type: 'hasMany', model: 'Mdc.model.RegisterType', associationKey: 'registerTypes',
                getTypeDiscriminator: function (node) {
                    return 'Mdc.model.RegisterType';
                }
            }  ,
            {name: 'deviceLifeCycleEffectiveTimeShiftPeriod', type: 'hasOne', model: 'Mdc.model.field.TimePeriod'}
        ],

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes',
        reader: {
            type: 'json'
        }
    },

    isDataLoggerSlave: function() {
        return this.get('deviceTypePurpose') === 'DATALOGGER_SLAVE';
    },
    isMultiElementSlave: function() {
        return this.get('deviceTypePurpose') === 'MULTI_ELEMENT_SLAVE';
    }

});
