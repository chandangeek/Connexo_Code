Ext.define('Mdc.view.setup.deviceloadprofilechannels.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfileChannelsPreview',
    itemId: 'deviceLoadProfileChannelsPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.PreviewForm',
        'Mdc.view.setup.deviceloadprofilechannels.ActionMenu'
    ],
    layout: 'fit',
    frame: true,

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

    items: {
        xtype: 'deviceLoadProfileChannelsPreviewForm'
    }
});
