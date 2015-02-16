Ext.define('Isu.model.CreationRule', {
    extend: 'Ext.data.Model',
    requires: [
        'Isu.model.CreationRuleAction'
    ],
    idProperty: 'id',
    fields: [
        {
            name: 'id',
            type: 'int',
            useNull: true
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'parameters',
            type: 'auto'
        },
        {
            name: 'comment',
            type: 'string'
        },
        {
            name: 'creationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'modificationDate',
            dateFormat: 'time',
            type: 'date'
        },
        {
            name: 'version',
            type: 'int'
        },
        {
            name: 'template',
            type: 'auto'
        },
        {
            name: 'issueType',
            type: 'auto'
        },
        {
            name: 'reason',
            type: 'auto'
        },
        {
            name: 'dueIn',
            type: 'auto'
        },
        {
            name: 'title',
            mapping: 'name'
        },
        {
            name: 'issueType_name',
            mapping: 'issueType.name'
        },
        {
            name: 'reason_name',
            mapping: 'reason.name'
        },
        {
            name: 'template_name',
            mapping: 'template.name'
        },
        {
            name: 'due_in',
            mapping: function (data) {
                var dueIn = '';

                if (data.dueIn && data.dueIn.number) {
                    dueIn =  data.dueIn.number + ' ' + data.dueIn.type;
                }

                return dueIn;
            }
        }
    ],

    associations: [
        {
            name: 'actions',
            type: 'hasMany',
            model: 'Isu.model.CreationRuleAction',
            associationKey: 'actions'
        }
    ],

    proxy: {
        type: 'rest',
        url: '/api/isu/creationrules',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
