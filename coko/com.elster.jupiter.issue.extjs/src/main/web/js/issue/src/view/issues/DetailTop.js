Ext.define('Isu.view.issues.DetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.issue-detail-top',
    requires: [
        'Isu.view.issues.ActionMenu',
        'Isu.privileges.Issue',
        'Isu.privileges.Device'
    ],
    layout: 'hbox',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'issue-detail-title',
                title: 'Details',
                ui: 'medium',
                flex: 1
            },
            {
                xtype: 'button',
                margin: '5 0 0 0',
                itemId: 'issue-detail-top-actions-button',
                text: Uni.I18n.translate('general.actions', 'ISU', 'Actions'),
                privileges: Ext.Array.merge(Isu.privileges.Issue.adminDevice, Isu.privileges.Device.viewDeviceCommunication),
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