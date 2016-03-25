Ext.define('Uni.model.WhatsGoingOn', {
    extend: 'Ext.data.Model',
    idProperty: 'internalId',
    fields: [
        'type',
        'id',
        'description',
        {
            name: 'due date',
            type: 'date'
        },
        'severity',
        'assignee',
        'assigneeIsCurrentUser',
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
                    id: record.id,
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