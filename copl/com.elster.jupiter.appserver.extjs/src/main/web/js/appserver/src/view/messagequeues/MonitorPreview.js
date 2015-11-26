Ext.define('Apr.view.messagequeues.MonitorPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.monitor-preview',
    router: null,
    requires: [
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
                    xtype: 'monitor-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'monitor-preview-form'
        };
        me.callParent(arguments);
    }

});


