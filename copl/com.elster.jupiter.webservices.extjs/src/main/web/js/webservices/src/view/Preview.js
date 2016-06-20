Ext.define('Wss.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.webservices-preview',
    requires: [
        'Wss.view.PreviewForm',
        'Wss.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'WSS', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                itemId: 'webservicePreviewMenuButton',
                privileges: Wss.privileges.Webservices.admin,
                menu: {
                    xtype: 'webservices-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'webservices-preview-form',
            itemId: 'webservices-grid-preview-form',
        };
        me.callParent(arguments);
    }

});