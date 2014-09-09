Ext.define('Mdc.model.Logbook', {
    extend: 'Ext.data.Model',
    alias: 'widget.mdc-model',

    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'name',
            displayValue: 'Name',
            type: 'string'
        },
        {
            name: 'obis',
            displayValue: 'OBIS',
            type: 'string'
        },
        {
            name: 'isInUse',
            displayValue: 'Is in use',
            type: 'boolean'
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



