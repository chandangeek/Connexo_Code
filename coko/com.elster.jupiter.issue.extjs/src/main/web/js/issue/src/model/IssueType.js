Ext.define('Isu.model.IssueType', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'uid',
            type: 'text'
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
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});