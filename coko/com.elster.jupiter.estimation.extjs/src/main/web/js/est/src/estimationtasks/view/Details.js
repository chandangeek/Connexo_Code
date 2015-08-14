Ext.define('Est.estimationtasks.view.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.estimationtasks-details',
    requires: [
        'Est.estimationtasks.view.ActionMenu',
        'Est.estimationtasks.view.DetailForm',
        'Est.estimationtasks.view.SideMenu'
    ],

    router: null,
    taskId: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.overview', 'EST', 'Overview'),
                flex: 1,
                items: {
                    xtype: 'estimationtasks-detail-form',
                    itemId: 'estimationtasks-detail-form',
                    margin: '0 0 0 100'
                }
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'EST', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                margin: '20 0 0 0',
                menu: {
                    xtype: 'estimationtasks-action-menu',
                    itemId: 'estimationtasks-action-menu'
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
                        xtype: 'estimationtasks-side-menu',
                        itemId: 'estimationtasks-side-menu',
                        router: me.router,
                        taskId: me.taskId
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
