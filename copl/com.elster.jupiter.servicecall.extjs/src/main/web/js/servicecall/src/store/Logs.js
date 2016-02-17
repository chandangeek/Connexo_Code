Ext.define('Scs.store.Logs', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    //model: 'Scs.model.Log',
    //pageSize: 50,
   /* proxy: {
        type: 'rest',
        urlTpl: '/api/scs/{internalId}/logs',
        reader: {
            type: 'json',
            root: 'scs'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{internalId}', params.internalId);
        }
    }*/

    fields: [
        {name: 'loglevel'},
        {name: 'message'},
        {name: 'timestamp'}
    ],
    data: [
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
        {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'}

    ]
});
