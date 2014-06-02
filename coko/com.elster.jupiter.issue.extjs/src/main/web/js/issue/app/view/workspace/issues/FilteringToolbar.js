Ext.define('Isu.view.workspace.issues.FilteringToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Skyline.button.TagButton'
    ],
    alias: 'widget.filtering-toolbar',
    itemId: 'filtering-toolbar',
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
            btnClass = 'Skyline.button.TagButton',
            container = me.getContainer();


        container.removeAll();

        if (filter.get('assignee')) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-assignee',
                text: 'Assignee: ' + filter.get('assignee').get('name'),
                target: 'assignee'
            }));

        }

        if (filter.get('reason')) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-reason',
                text: 'Reason: ' + filter.get('reason').get('name'),
                target: 'reason'
            }));
        }

        if (filter.get('department')) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-department',
                text: 'Department: ' + filter.get('department').get('name'),
                target: 'department'
            }));
        }

        if (filter.get('meter')) {
            container.add(Ext.create(btnClass, {
                itemId: 'filter-by-meter',
                text: 'Meter: ' + filter.get('meter').get('name'),
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