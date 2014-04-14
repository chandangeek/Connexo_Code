Ext.define('Isu.model.Licensing', {
    extend: 'Ext.data.Model',
    alias: 'widget.lic-model',
    fields: [
        {
            name: 'applicationtag',
            type: 'text'
        },
        {
            name: 'id',
            convert: function(value, record) {
                return record.get('applicationtag');
            },
            type: 'text'
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
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/sam/license',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
