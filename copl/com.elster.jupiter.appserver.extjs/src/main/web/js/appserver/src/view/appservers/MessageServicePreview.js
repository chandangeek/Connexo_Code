Ext.define('Apr.view.appservers.MessageServicePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.msg-service-preview',
    requires: [
        'Apr.view.appservers.MessageServicePreviewForm',
        'Apr.view.appservers.MessageServicesActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'APR', 'Actions'),
                privileges: Apr.privileges.AppServer.admin,
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'message-services-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'msg-service-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }
});