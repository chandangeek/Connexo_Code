Ext.define('Cfg.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        'loglevel', 'message',
        {
            name: 'timestamp',
            dateFormat: 'time',
            type: 'date'
        }
    ]
});
