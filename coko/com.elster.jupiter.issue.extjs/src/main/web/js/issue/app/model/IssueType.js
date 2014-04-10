Ext.define('Isu.model.IssueType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uid',
            type: 'int'
        },
        {
            name: 'name',
            type: 'text'
        }
    ],

    idProperty: 'uid',

    proxy: {
        type: 'rest',
        url: '/api/isu/issuetypes',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});