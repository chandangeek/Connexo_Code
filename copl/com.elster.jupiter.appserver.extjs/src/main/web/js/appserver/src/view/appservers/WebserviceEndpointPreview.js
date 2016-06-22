Ext.define('Apr.view.appservers.WebserviceEndpointPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.webservice-preview',
    router: null,

    requires: [
        'Apr.view.appservers.WebserviceEndpointPreviewForm',
        'Apr.view.appservers.WebserviceEndpointActionMenu'
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
                    xtype: 'apr-webservices-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'webservice-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }
});