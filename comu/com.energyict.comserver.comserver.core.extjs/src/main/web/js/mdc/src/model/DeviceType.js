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
        {name: 'canBeDirectlyAddressed', type: 'boolean', useNull: true},
        {name: 'deviceProtocolPluggableClass', type: 'string', useNull: true},
        {name: 'deviceProtocolPluggableClassId', type: 'number', useNull: true},
        {name: 'registerTypes'},
        {name: 'deviceLifeCycleId'},
        {name: 'deviceLifeCycleName'},
        {name: 'version', type: 'number', useNull: true}
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
