Ext.define('Isu.model.Assignee', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'type',
            displayValue: 'Type',
            type: 'auto'
        },
        {
            name: 'title',
            displayValue: 'title',
            type: 'auto'
        }
    ],

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
