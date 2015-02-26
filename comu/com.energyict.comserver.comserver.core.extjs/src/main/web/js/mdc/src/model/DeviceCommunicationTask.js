Ext.define('Mdc.model.DeviceCommunicationTask', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'comTask'},
        {name: 'connectionMethod', type: 'string'},
        {name: 'connectionDefinedOnDevice', type: 'boolean'},
        {name: 'connectionStrategy', type: 'string'},
        {name: 'connectionStrategyKey', type: 'string'},
        {name: 'nextCommunication', dateFormat: 'time', type: 'date'},
        {name: 'lastCommunication', dateFormat: 'time', type: 'date'},
        {name: 'urgency', type: 'int'},
        {name: 'securitySettings', type: 'string'},
        {name: 'protocolDialect', type: 'string'},
        'temporalExpression',
        {name: 'scheduleType', type: 'string'},
        {name: 'scheduleTypeKey', type: 'string'},
        {name: 'scheduleName', type: 'string'},
        {name: 'plannedDate', dateFormat: 'time', type: 'date'},
        {name: 'status', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/ddr/devices/{mrid}/comtasks'
    }
});