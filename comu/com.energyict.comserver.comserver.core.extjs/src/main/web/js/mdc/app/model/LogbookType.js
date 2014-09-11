Ext.define('Mdc.model.LogbookType', {
    extend: 'Ext.data.Model',
    alias: 'widget.mdc-model',

    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', useNull: true, type: 'string'},
        {name: 'obisCode', type: 'string', useNull: true},
        {name: 'isInUse', type: 'boolean', useNull: true
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/mds/logbooktypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});



