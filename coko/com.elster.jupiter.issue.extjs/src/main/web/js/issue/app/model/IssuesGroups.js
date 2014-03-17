Ext.define('Isu.model.IssuesGroups', {
    extend: 'Ext.data.Model',    
    fields: ['reason', 'number' ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issue/groupedlist',
        reader: {
            type: 'json',
            root: 'data',
            totalProperty: 'totalCount'
        }
    }
});