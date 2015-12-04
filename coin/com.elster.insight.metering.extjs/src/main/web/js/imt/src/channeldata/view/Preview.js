Ext.define('Imt.channeldata.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.channelsPreview',
    itemId: 'channelsPreview',
    requires: [
        'Imt.channeldata.view.PreviewForm',
        'Imt.channeldata.view.ActionMenu'
    ],
    layout: 'fit',
    frame: true,
    usagepoint: null,
    router: null,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'channelsActionMenu'
            }
        }
    ],


    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'channelsPreviewForm',
            router: me.router,
            usagepoint: me.usagepoint
        };

        me.callParent(arguments);
    }
});
