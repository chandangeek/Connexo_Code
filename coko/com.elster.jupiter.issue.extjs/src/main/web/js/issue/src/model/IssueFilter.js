Ext.define('Isu.model.IssueFilter', {
    extend: 'Uni.component.filter.model.Filter',

    requires: [
        'Isu.model.Assignee',
        'Isu.model.IssueMeter',
        'Isu.model.IssueReason',
        'Isu.model.IssueStatus'
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
            name: 'assignee',
            setterName: 'setAssignee',
            getterName: 'getAssignee'
        },
        {
            model: 'Isu.model.IssueReason',
            associationKey: 'reason',
            name: 'reason',
            setterName: 'setReason',
            getterName: 'getReason'
        },
        {
            model: 'Isu.model.IssueMeter',
            associationKey: 'meter',
            name: 'meter',
            setterName: 'setMeter',
            getterName: 'getMeter'
        }
    ],

    /**
     * @override
     * @returns {String[]}
     */
    getFields: function () {
        var fields = this.callParent();
        fields.push('assigneeId');
        fields.push('assigneeType');
        return fields;
    },

    /**
     * @override
     * @returns {*|Object}
     */
    getPlainData: function () {
        var data = this.callParent(),
            assignee;

        if (data.assignee) {
            assignee = data.assignee.split(':');
            data.assigneeId = assignee[0];
            data.assigneeType = assignee[1];
        }

        return data;
    }
});
