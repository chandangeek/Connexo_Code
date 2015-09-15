Ext.define('Dsh.store.filter.CompletionCodes', {
    extend: 'Ext.data.Store',
    fields: ['localizedValue', 'completionCode'],
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/completioncodes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'completionCodes'
        }
    }
});