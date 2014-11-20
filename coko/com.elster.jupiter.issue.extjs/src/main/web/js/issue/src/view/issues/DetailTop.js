Ext.define('Isu.view.issues.DetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.issue-detail-top',
    requires: [
        'Isu.view.issues.ActionMenu'
    ],
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'issue-detail-top-title',
                title: '&nbsp;',
                ui: 'large',
                flex: 1
            },
            {
                xtype: 'button',
                itemId: 'issue-detail-top-actions-button',
                text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
                hidden:  Uni.Auth.hasAnyPrivilege(['privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue',
                    'privilege.administrate.device','privilege.view.device','privilege.view.scheduleDevice']),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'issues-action-menu',
                    itemId: 'issue-detail-action-menu',
                    predefinedItems: null,
                    router: me.router
                },
                listeners: {
                    click: function () {
                        this.showMenu();
                    }
                }
            }
        ];

        me.callParent(arguments);
    }
});