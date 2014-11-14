Ext.define('Dxp.view.tasks.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-details',
    requires: [
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.PreviewForm',
        'Dxp.view.tasks.ActionMenu'
    ],

    router: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                flex: 1,
                items: {
                    xtype: 'tasks-preview-form',
                    margin: '0 0 0 100'
                }
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'DES', 'Actions'),
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
                title: Uni.I18n.translate('general.dataExportTasks', 'DES', 'Data export tasks'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'tasks-menu',
                        itemId: 'tasks-view-menu',
                        router: me.router,
                        toggle: 0
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});

