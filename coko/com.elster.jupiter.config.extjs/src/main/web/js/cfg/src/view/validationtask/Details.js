Ext.define('Cfg.view.validationtask.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-validation-tasks-details',
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
                title: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                flex: 1,
                items: {
                    xtype: 'cfg-tasks-preview-form',
                    itemId: 'frm-validation-task-details',
                    margin: '0 0 0 100'
                }
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'CFG', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                margin: '20 0 0 0',
                menu: {
                    xtype: 'cfg-validation-tasks-action-menu'
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
                        xtype: 'cfg-tasks-menu',
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

