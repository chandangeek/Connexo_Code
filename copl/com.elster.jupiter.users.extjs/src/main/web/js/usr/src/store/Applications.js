Ext.define('Usr.store.Applications', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Application',
    pageSize: 500,
    sorters: {
        property: 'translatedName',
        direction: 'ASC'
    }
});