Ext.define('Idv.model.Issue', {
    extend: 'Isu.model.Issue',

    proxy: {
        type: 'rest',
        url: '/api/idv/issues',
        reader: 'json'
    }
});