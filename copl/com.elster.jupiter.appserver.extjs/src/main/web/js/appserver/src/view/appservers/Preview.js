Ext.define('Apr.view.appservers.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.appservers-preview',
    router: null,
    requires: [
        'Apr.view.appservers.PreviewForm'
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
                    xtype: 'appservers-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'appservers-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }

});

