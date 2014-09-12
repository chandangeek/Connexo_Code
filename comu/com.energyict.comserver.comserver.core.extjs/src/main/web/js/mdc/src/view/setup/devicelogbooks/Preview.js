Ext.define('Mdc.view.setup.devicelogbooks.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLogbooksPreview',
    itemId: 'deviceLogbooksPreview',
    requires: [
        'Mdc.view.setup.devicelogbooks.PreviewForm',
        'Mdc.view.setup.devicelogbooks.ActionMenu'
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
                xtype: 'deviceLogbooksActionMenu'
            }
        }
    ],

    items: {
        xtype: 'deviceLogbooksPreviewForm'
    }
});
