Ext.define('Uni.model.WhatsGoingOn', {
    extend: 'Ext.data.Model',
    idProperty: 'internalId',
    fields: [
        'type',
        'issueType',
        'id',
        'description',
        {
            name: 'due date',
            type: 'date'
        },
        'severity',
        'assignee',
        'assigneeIsCurrentUser',
        'reference',
        'status',
        {
            name: 'internalId',
            mapping: function(record){
                return record.type + '_' + record.id;
            }
        },
        {
            name: 'displayValue',
            mapping: function(record){
                return {
                    type: record.type,
                    issueType: record.issueType,
                    id: record.id,
                    reference : record.reference,
                    description: record.description,
                    dueDate: record.dueDate,
                    severity: record.severity,
                    assignee: record.assignee,
                    status: record.status,
                    assigneeIsCurrentUser: record.assigneeIsCurrentUser
                }
            }
        }

    ]
});