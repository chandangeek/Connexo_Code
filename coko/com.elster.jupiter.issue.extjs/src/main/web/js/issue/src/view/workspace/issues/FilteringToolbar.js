Ext.define('Isu.view.workspace.issues.FilteringToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.TagButton'
    ],
    alias: 'widget.filtering-toolbar',
    title: 'Filters',
    name: 'filter',
    emptyText: 'None',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    /**
     * todo: I18n
     * @param filter Uni.component.filter.model.Filter
     */
    addFilterButtons: function (filter) {
        var me = this,
            btnClass = 'Uni.view.button.TagButton',
            container = me.getContainer(),
            assignee = filter.getAssignee().get('name'),
            reason = filter.getReason().get('name'),
            meter = filter.getMeter().get('name');


        container.removeAll();

        if (assignee) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-assignee',
                text: 'Assignee: ' + assignee,
                target: 'assignee'
            }));
        }

        if (reason) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-reason',
                text: 'Reason: ' + reason,
                target: 'reason'
            }));
        }

        if (meter) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-meter',
                text: 'Meter: ' + meter,
                target: 'meter'
            }));
        }

        if (filter.status().count()) {
            filter.status().each(function (status) {
                var c = container.add({
                    xtype: 'tag-button',
                    itemId: 'filter-by-status',
                    text: 'Status: ' + status.get('name'),
                    target: 'status',
                    targetId: status.getId()
                });
            });
        }
    }
});