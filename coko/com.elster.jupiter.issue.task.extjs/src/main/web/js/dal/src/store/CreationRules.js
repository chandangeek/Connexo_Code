Ext.define('Itk.store.CreationRules', {
    extend: 'Ext.data.Store',
    model: 'Itk.model.CreationRule',
    pageSize: 10,
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/itk/creationrules',
        reader: {
            type: 'json',
            root: 'creationRules'
        }
    }
});
