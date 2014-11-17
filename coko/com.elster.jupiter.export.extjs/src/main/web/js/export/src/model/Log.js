Ext.define('Dxp.model.Log', {
    extend: 'Ext.data.Model',
    fields: [
        'loglevel', 'message',
        {
            name: 'timestamp',
            mapping: function (data) {
                if (data.timestamp) {
                    return moment(data.timestamp).format('ddd, DD MMM YYYY HH:mm:ss');
                }
            }
        }
    ]
});
