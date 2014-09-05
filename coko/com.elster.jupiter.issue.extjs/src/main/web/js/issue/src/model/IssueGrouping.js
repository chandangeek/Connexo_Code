Ext.define('Isu.model.IssueGrouping', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'value',
            type: 'string'
        },
        {
            name: 'display',
            type: 'string'
        }
    ],
    idProperty: 'value'
});