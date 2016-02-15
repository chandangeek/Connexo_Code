Ext.define('Sct.store.ServiceCallTypes', {
    extend: 'Ext.data.Store',
    model: 'Sct.model.ServiceCallType',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/scs/servicecalltypes',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'serviceCallTypes'
        }
    }

});
