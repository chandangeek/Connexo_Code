Ext.define('Mdc.model.LogbookTypes', {
    extend: 'Ext.data.Model',

    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'obisCode',
            type: 'string'
        }
    ],

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/logbooktypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

