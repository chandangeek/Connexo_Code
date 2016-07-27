Ext.define('Ddv.store.Validations', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Ddv.model.Validation',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '/api/dvr/validationresults/devicegroups',

        appendId: false,
        reader: {
            type: 'json',
            root: 'summary',
            totalProperty: 'total'
        }
    }
});
