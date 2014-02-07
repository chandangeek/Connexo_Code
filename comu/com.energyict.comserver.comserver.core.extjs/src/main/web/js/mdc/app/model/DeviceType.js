Ext.define('Mdc.model.DeviceType', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.DeviceProtocol'
    ],
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'loadProfileCount', type: 'int', useNull: true},
        {name: 'registerCount', type: 'int', useNull: true},
        {name: 'logBookCount', type: 'int', useNull: true},
        {name: 'deviceConfigurationCount', type: 'int', useNull: true},
        {name: 'canBeGateway', type: 'boolean', useNull: true},
        {name: 'canBeDirectlyAddressable', type: 'boolean', useNull: true},
        {name: 'communicationProtocolName', type: 'string', useNull: true},
        {name: 'serviceCategory', type: 'string', useNull: true},
        {name: 'deviceFunction', type: 'string', useNull: true}
    ],

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes',
        reader: {
            type: 'json'
        }
    }

});
