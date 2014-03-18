Ext.define('Isu.model.IssueSort', {
    extend: 'Isu.component.sort.model.Sort',

    fields: [
        {
            name: 'dueDate',
            displayValue: 'Due date'
        },
        {
            name: 'creationDate',
            displayValue: 'Creation date'
        }
    ]
});
