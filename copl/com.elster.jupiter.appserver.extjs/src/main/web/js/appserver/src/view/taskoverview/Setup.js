Ext.define('Apr.view.taskoverview.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.task-overview-setup',
    router: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.taskOverview', 'APR', 'Task overview'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'task-overview-grid',
                        itemId: 'task-overview-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-tasks-found',
                        title: Uni.I18n.translate('taskOverview.empty.title', 'APR', 'No tasks found'),
                        reasons: [
                            Uni.I18n.translate('taskOverview.empty.list.item1', 'APR', 'There are no tasks in the system')
                        ],
                        stepItems: [
                        ]
                    },
                    previewComponent: {
                        xtype: 'task-preview',
                        itemId: 'task-preview'
                    }
                }
            ],
            dockedItems:  [
                {
                    dock: 'top',
                    xtype: 'taskFilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});
