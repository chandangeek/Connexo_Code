Ext.define('Sam.model.Licensing', {
    extend: 'Ext.data.Model',
    alias: 'widget.lic-model',
    fields: [
        {
            name: 'applicationkey',
            type: 'text'
        },
        {
            name: 'id',
            convert: function(value, record) {
                return record.get('applicationkey');
            },
            type: 'text'
        },
        {
            convert: function(v, rec) {
                if (v === 'BPM'){
                    return Uni.I18n.translate('BPM', 'SAM', 'Flow');
                }else if (v === 'YFN'){
                    return Uni.I18n.translate('YFN', 'SAM', 'Facts');
                }else if (v === 'MDC'){
                    return Uni.I18n.translate('MDC', 'SAM', 'MultiSense');
                }
                return v;
            },
            name: 'applicationname',
            type: 'text'
        },
        {
            name: 'status',
            type: 'auto'
        },
        {
            name: 'expires',
            dateFormat: 'time',
            type: 'date'
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
        url: '/api/lic/license',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});

