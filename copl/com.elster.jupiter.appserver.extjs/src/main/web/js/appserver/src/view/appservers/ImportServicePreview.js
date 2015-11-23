Ext.define('Apr.view.appservers.ImportServicePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.import-service-preview',
    router: null,

    requires: [
        'Apr.view.appservers.ImportServicePreviewForm'
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
                    xtype: 'apr-import-services-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'import-service-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }
});