Ext.define('Est.estimationtasks.view.Log', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.estimationtasks-log-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Est.estimationtasks.view.LogGrid',
        'Est.estimationtasks.view.LogPreview',
        'Est.estimationtasks.view.LogSideMenu'
    ],
    task: null,
    runStartedOn: null,
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'estimationtasks-log-menu',
                        itemId: 'estimationtasks-log-menu',
                        taskId: me.taskId,
                        router: me.router,
                        occurence: me.occurenceId
                    }
                ]
            }
        ];
        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('estimationtasks.general.log', 'EST', 'Log'),
            items: [
                {
                    xtype: 'estimationtasks-log-preview',
                    router: me.router,
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'estimationtasks-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('estimationtasks.log.empty.title', 'EST', 'No logs found'),
                        reasons: [
                            Ext.String.format(
                                Uni.I18n.translate('estimationtasks.log.empty.list.item1', 'EST', "Estimation task '{0}' started on {1} did not create any logs."),
                                me.task.get('name'),
                                me.runStartedOn
                            )
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});