Ext.define('Dxp.model.EndDeviceEventTypePart', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {   name: 'mnemonic',
            type: 'string'
        },
        {   name: 'value',
            type: 'int'
        },
        {   name: 'displayName',
            type: 'string'
        }
    ]
});