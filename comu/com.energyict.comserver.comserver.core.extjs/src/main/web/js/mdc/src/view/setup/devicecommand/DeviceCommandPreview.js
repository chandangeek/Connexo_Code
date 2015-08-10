Ext.define('Mdc.view.setup.devicecommand.DeviceCommandPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommandPreview',
    requires: [
        'Mdc.view.setup.devicecommand.DeviceCommandPreviewForm',
        'Uni.property.form.Property',
        'Mdc.view.setup.devicecommand.widget.ActionMenu'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    frame: true,
    header: {
        height: 37
    },
    tools: [
        {
            xtype: 'button',
            itemId: 'commandsPreviewActionButton',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            hidden: Mdc.privileges.DeviceCommands.executeCommands,
            iconCls: 'x-uni-action-iconD',
            hidden: true,
            menu: {
                xtype: 'device-command-action-menu'
            }
        }
    ],
    items: [
        {
            xtype: 'deviceCommandPreviewForm'
        },
        {
            itemId: 'previewPropertiesHeader'
        },
        {
            xtype: 'panel',
            ui: 'medium',
            itemId: 'previewPropertiesPanel',
            items: [
                {
                    xtype: 'property-form',
                    isEdit: false,
                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 200,
                        columnWidth: 0.5
                    }
                }
            ]
        }
    ]
});


