Ext.define('Mdc.model.DeviceProtocol', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string', useNull: true},
        {name: 'deviceFunction', type: 'string', useNull: true},
        {name: 'serviceCategory', type: 'string', useNull: true}
    ]
})
