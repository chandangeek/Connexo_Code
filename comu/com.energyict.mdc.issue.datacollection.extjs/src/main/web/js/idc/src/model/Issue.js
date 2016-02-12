Ext.define('Idc.model.Issue', {
    extend: 'Isu.model.Issue',

    proxy: {
        type: 'rest',
        url: '/api/idc/issues',
        reader: 'json'
    }
});
