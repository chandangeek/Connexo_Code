Ext.define('Mdc.view.setup.devicechannels.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreview',
    itemId: 'deviceLoadProfileChannelsPreview',
    requires: [
        'Mdc.view.setup.devicechannels.PreviewForm',
        'Mdc.view.setup.devicechannels.ActionMenu'
    ],
    layout: 'fit',
    frame: true,
    device: null,
    router: null,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceLoadProfileChannelsActionMenu'
            }
        }
    ],


    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'deviceLoadProfileChannelsPreviewForm',
            router: me.router,
            device: me.device
        };

        me.callParent(arguments);
    }
});
