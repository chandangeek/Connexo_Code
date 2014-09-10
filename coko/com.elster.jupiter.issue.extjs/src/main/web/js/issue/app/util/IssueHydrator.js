Ext.define('Isu.util.IssueHydrator', {
    extend: 'Uni.util.Hydrator',

    extract: function () {
        var me = this,
            data = me.callParent(arguments),
            assignee;

        if (data.assignee) {
            assignee = data.assignee.split(':');
            data.assigneeId = assignee[0];
            data.assigneeType = assignee[1];
            delete data.assignee;
        }

        delete data.id;

        return data;
    },

    hydrate: function (data, object) {
        var me = this,
            assignee = {},
            assigneeId,
            assigneeType;

        assigneeId = Ext.Array.findBy(data, function (item) {
            return item.property === 'assigneeId';
        });
        assigneeType = Ext.Array.findBy(data, function (item) {
            return item.property === 'assigneeType';
        });
        if (assigneeId && assigneeType) {
            assignee.property = 'assignee';
            assignee.value = assigneeId.value + ':' + assigneeType.value;
            data.push(assignee);
            Ext.Array.remove(data, assigneeId);
            Ext.Array.remove(data, assigneeType);
        }

        me.callParent([data, object]);
        return object;
    }
});