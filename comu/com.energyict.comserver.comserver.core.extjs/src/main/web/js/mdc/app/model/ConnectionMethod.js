Ext.define('Mdc.model.ConnectionMethod', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'direction', type: 'string', useNull: true},
        {name: 'simultaneousConnectionAllowed', type: 'string', useNull: true},
        {name: 'setAsDefault', type: 'string', useNull: true},
        {name: 'portPool', type: 'string', useNull: true},
        {name: 'connectionType', type: 'string', useNull: true},
        {name: 'host', type: 'string', useNull: true},
        {name: 'portNumber', type: 'string', useNull: true},
        {name: 'connectionTimeout', type: 'string', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods'
    }
});