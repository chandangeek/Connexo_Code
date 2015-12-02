Ext.define('Imt.view.setup.devicechannels.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreview',
    itemId: 'deviceLoadProfileChannelsPreview',
    requires: [
        'Imt.view.setup.devicechannels.PreviewForm',
        'Imt.view.setup.devicechannels.ActionMenu'
    ],
    layout: 'fit',
    frame: true,
    device: null,
    router: null,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
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
