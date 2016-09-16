Ext.define('Usr.store.Applications', {
    extend: 'Ext.data.Store',
    model: 'Usr.model.Application',
    pageSize: 1000,
    sorters: {
        property: 'sortingfield',
        direction: 'ASC'
    }
});