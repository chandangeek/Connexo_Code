Ext.define('Isu.model.Licensing', {
    extend: 'Ext.data.Model',
    alias: 'widget.lic-model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'application',
            type: 'auto'
        },
        {
            name: 'status',
            type: 'auto'
        },
        {
            name: 'expires',
            type: 'auto'
        },
        {
            name: 'version',
            type: 'auto'
        },
        {
            name: 'description',
            type: 'auto'
        },
        {
            name: 'type',
            type: 'auto'
        },
        {
            name: 'validfrom',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'graceperiod',
            type: 'auto'
        },
        {
            name: 'content',
            type: 'auto'
        },
        {
            name: 'versions',
            type: 'auto'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/apps/issue/app/licenses.json',
        reader: {
            type: 'json',
            root: 'licenses'
        }
    }
});
