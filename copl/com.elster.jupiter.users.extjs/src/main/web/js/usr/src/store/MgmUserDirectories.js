Ext.define('Usr.store.MgmUserDirectories', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.MgmUserDirectory',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/fir/importservices',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'importSchedules'
        }
    }

});
