Ext.define('Mtr.model.IssuesGroups', {
    extend: 'Ext.data.Model',    
    fields: ['reason', 'number' ],

    proxy: {
        type: 'rest',
        url: '/api/isu/issue/groupedlist',
        reader: {
            type: 'json',
            root: 'groups',
            totalProperty: 'totalCount'
        }
    }
});