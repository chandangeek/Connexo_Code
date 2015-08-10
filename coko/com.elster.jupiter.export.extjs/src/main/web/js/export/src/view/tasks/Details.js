Ext.define('Dxp.view.tasks.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-details',
    requires: [
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.PreviewForm',
        'Dxp.view.tasks.ActionMenu'
    ],

    router: null,
    taskId: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.overview', 'DES', 'Overview'),
                flex: 1,
                items: {
                    xtype: 'dxp-tasks-preview-form',
                    margin: '0 0 0 100'
                }
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'DES', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                margin: '20 0 0 0',
                menu: {
                    xtype: 'dxp-tasks-action-menu'
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
                        xtype: 'dxp-tasks-menu',
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

