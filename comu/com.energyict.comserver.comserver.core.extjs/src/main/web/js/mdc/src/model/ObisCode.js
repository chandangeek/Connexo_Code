
Ext.define('Mdc.model.ObisCode', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'name', type: 'string'}
    ],

    proxy: {
        type: 'rest',
        url: '../../api/mds/obiscodebyreadingtype',
        reader: {
            type: 'json',
            root: 'obisValue'
        }
    }
});