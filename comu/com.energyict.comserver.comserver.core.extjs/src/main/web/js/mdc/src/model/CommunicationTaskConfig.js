/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.CommunicationTaskConfig', {
    extend: 'Uni.model.ParentVersion',
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'comTask', type: 'auto', useNull: true, defaultValue: null},
        {name: 'securityPropertySet', type: 'auto', useNull: true, defaultValue: null},
        {name: 'partialConnectionTask', type: 'auto', useNull: true, defaultValue: null},
        {name: 'protocolDialectConfigurationProperties', type: 'auto', useNull: true, defaultValue: null},
        {name: 'priority', type: 'int', useNull: true},
        {name: 'suspended', type: 'boolean', useNull: true},
        {name: 'ignoreNextExecutionSpecsForInbound', type: 'boolean', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/comtaskenablements'
    }
});