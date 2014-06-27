Ext.define('Bpm.model.Startup', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'url', type: 'string', useNull: true},
        {name: 'user', type: 'string', useNull: true},
        {name: 'password', type: 'string', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/bpm/runtime/startup',
        appendId: false,
        reader: {
            type: 'json'
        }
    }
});