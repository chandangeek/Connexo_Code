Ext.define('Isu.view.workspace.issues.FilteringToolbar', {
    extend: 'Skyline.panel.FilterToolbar',
    requires: [
        'Skyline.button.TagButton'
    ],
    alias: 'widget.filtering-toolbar',

    title: 'Filters',
    name: 'filter',
    emptyText: 'None',

    /**
     * todo: I18n
     * @param filter Uni.component.filter.model.Filter
     */
    addFilterButtons: function (filter) {
        var me = this,
            btnClass = 'Skyline.button.TagButton',
            container =  me.getContainer();

        container.removeAll();

        if (filter.get('assignee')) {
            container.add(Ext.create(btnClass, {
                text: 'Assignee: ' + filter.get('assignee').get('name'),
                target: 'assignee'
            }));
        }

        if (filter.get('reason')) {
            container.add(Ext.create(btnClass, {
                text: 'Reason: ' + filter.get('reason').get('name'),
                target: 'reason'
            }));
        }

        if (filter.get('department')) {
            container.add(Ext.create(btnClass, {
                text: 'Department: ' + filter.get('department').get('name'),
                target: 'department'
            }));
        }

        if (filter.get('meter')) {
            container.add(Ext.create(btnClass, {
                text: 'Meter: ' + filter.get('meter').get('name'),
                target: 'meter'
            }));
        }

        if (filter.status().count()) {
            filter.status().each(function (status) {
                container.add(Ext.create(btnClass, {
                    text: 'Status: ' + status.get('name'),
                    target: 'status',
                    targetId: status.getId()
                }));
            });
        }
    }
});