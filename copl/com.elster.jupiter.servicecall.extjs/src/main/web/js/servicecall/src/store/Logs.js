Ext.define('Scs.store.Logs', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    //model: 'Scs.model.Log',
    //pageSize: 50,
    proxy: {
        type: 'rest',
        urlTpl: '/api/scs/servicecalls/{internalId}/logs',
        reader: {
            type: 'json',
            root: 'scs'
        },

        setUrl: function (internalId) {
            this.url = this.urlTpl.replace('{internalId}', internalId);
        }
    }

    //fields: [
    //    {name: 'loglevel'},
    //    {name: 'message'},
    //    {name: 'timestamp'}
    //],
    //data: [
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'},
    //    {loglevel: 'WARNING', timestamp: '18/10/2015', message: 'Message ENZO'}
    //
    //]
});
