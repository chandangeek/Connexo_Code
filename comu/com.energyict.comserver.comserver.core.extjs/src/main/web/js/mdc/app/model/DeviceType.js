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
        {name: 'registerTypes'}
    ],
    associations: [
            {name: 'registerTypes', type: 'hasMany', model: 'Mdc.model.RegisterType', associationKey: 'registerTypes',
                getTypeDiscriminator: function (node) {
                    return 'Mdc.model.RegisterType';
                }
            }
        ],

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes',
        reader: {
            type: 'json'
        }
    }

});
