Ext.define('Mdc.view.setup.devicegroup.DeviceGroupPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'deviceGroupPreview',
    alias: 'widget.deviceGroup-preview-form',
    frame: true,
    requires: [
        'Mdc.view.setup.devicegroup.DeviceGroupActionMenu',
        'Mdc.view.setup.devicegroup.PreviewForm'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'device-group-action-menu'
            }
        }
    ],

    items: {
        xtype: 'devicegroups-preview-form',
        itemId: 'deviceGroupPreviewForm'
    }
});
