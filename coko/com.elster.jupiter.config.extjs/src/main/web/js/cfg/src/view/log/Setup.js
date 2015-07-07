Ext.define('Cfg.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-log-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.view.log.Menu',
        'Cfg.view.log.Grid',
        'Cfg.view.log.Preview'
    ],
    task: null,
    runStartedOn: null,
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('validationTasks.general.validationTask', 'CFG', 'Validation task'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'log-menu',
                        itemId: 'log-view-menu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('validationTasks.general.log', 'CFG', 'Log'),
            items: [
                {
                    xtype: 'cfg-log-preview',
                    router: me.router,
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'cfg-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('validationTasks.log.empty.title', 'CFG', 'No logs found'),
                        reasons: [
                            Ext.String.format(Uni.I18n.translate('validationTasks.log.empty.list', 'CFG', "Validation task '{0}' started on {1} did not create any logs."), me.task.get('name'), me.runStartedOn)
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
