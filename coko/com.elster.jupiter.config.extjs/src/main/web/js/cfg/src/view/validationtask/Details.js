Ext.define('Cfg.view.validationtask.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.validation-tasks-details',
    requires: [
        'Cfg.view.validationtask.Menu',
        'Cfg.view.validationtask.PreviewForm',
        'Cfg.view.validationtask.ActionMenu'
    ],

    router: null,
    taskId: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('validationTasks.general.overview', 'CFG', 'Overview'),
                flex: 1,
                items: {
                    xtype: 'tasks-preview-form',
                    itemId: 'detail-tasks-preview-form',
                    margin: '0 0 0 100'
                }
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('validationTasks.general.actions', 'CFG', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                margin: '20 0 0 0',
                menu: {
                    xtype: 'tasks-action-menu'
                }
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'tasks-menu',
                        itemId: 'tasks-view-menu',
                        router: me.router,
                        taskId: me.taskId
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});

