Ext.define('Isu.model.IssueFilter', {
    extend: 'Uni.component.filter.model.Filter',

    requires: [
        'Isu.model.IssueStatus',
        'Isu.model.Assignee'
    ],

    hasMany: {
        model: 'Isu.model.IssueStatus',
        associationKey: 'status',
        name: 'status'
    },

    hasOne: [
        {
            model: 'Isu.model.Assignee',
            associationKey: 'assignee',
            name: 'assignee'
        },
        {
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason'
        }
    ],

    /**
     * @override
     * @returns {String[]}
     */
    getFields: function() {
        var fields = this.callParent();
        fields.push('assigneeId');
        fields.push('assigneeType');

        return fields;
    },

    /**
     * @override
     * @returns {*|Object}
     */
    getPlainData: function() {
        var data = this.callParent();
        var assignee = this.get('assignee');

        if (assignee) {
            data.assigneeId = assignee.get('id');
            data.assigneeType = assignee.get('type');
        }

        return data;
    }
});
