Ext.define('Dal.view.DetailTop', {
    extend: 'Ext.container.Container',
    alias: 'widget.alarm-detail-top',
    requires: [
        //   'Isu.view.issues.ActionMenu',
        'Dal.privileges.Alarm'
    ],
    layout: 'hbox',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'alarm-detail-title',
                title: Uni.I18n.translate('general.details', 'DAL', 'Details'),
                ui: 'medium',
                flex: 1
            },
            /* {
             xtype: 'uni-button-action',
             margin: '5 0 0 0',
             itemId: 'issue-detail-top-actions-button',
             privileges: Ext.Array.merge(Isu.privileges.Issue.adminDevice, Isu.privileges.Device.viewDeviceCommunication),
             menu: {
             xtype: 'issues-action-menu',
             itemId: 'issue-detail-action-menu',
             router: me.router
             },
             listeners: {
             click: function () {
             this.showMenu();
             }
             }
             }*/
        ];

        me.callParent(arguments);
    }
});