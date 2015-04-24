Ext.define('Est.estimationtasks.view.History', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.estimationtasks-history',
    requires: [
        'Est.estimationtasks.view.SideMenu',
        'Est.estimationtasks.view.HistoryFilterForm',
        'Est.estimationtasks.view.HistoryGrid',
        'Est.estimationtasks.view.HistoryPreview',
        'Uni.component.filter.view.FilterTopPanel',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    router: null,
    taskId: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'estimationtasks-side-menu',
                        itemId: 'estimationtasks-side-menu',
                        taskId: me.taskId,
                        router: me.router
                    },
                    {
                        xtype: 'estimationtasks-history-filter-form',
                        itemId: 'side-filter',
                        router: me.router
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('estimationtasks.general.history', 'EST', 'History'),
            items: [
                {
                    xtype: 'filter-top-panel',
                    itemId: 'estimationtasks-history-filter-top-panel'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'estimationtasks-history-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('estimationtasks.estimationtasksHistory.empty.title', 'EST', 'No estimation history found'),
                        reasons: [
                            Uni.I18n.translate('estimationtasks.estimationtasksHistory.empty.list.item1', 'EST', 'There is no history available for this estimation task.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'estimationtasks-history-preview'
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});
