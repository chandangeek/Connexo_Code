Ext.define('Mdc.view.setup.deviceregisterconfiguration.GeneralPreview', {
    extend: 'Ext.panel.Panel',

    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.ActionMenu'
    ],

    frame: true,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            itemId: 'gridPreviewActionMenu',
            menu: {
                xtype: 'deviceRegisterConfigurationActionMenu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
    }
});


