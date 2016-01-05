Ext.define('Mdc.view.setup.devicecommand.DeviceCommandAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-command-add',
    requires: [
        'Mdc.view.setup.devicecommand.widget.CommandForm',
        'Uni.property.form.Property',
        'Uni.util.FormErrorMessage'
    ],
    device: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'device-command-add-panel',
                title: Uni.I18n.translate('deviceCommand.add.title', 'MDC', 'Add command'),
                layout: {
                    type: 'vbox'
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        itemId: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'device-command-add-form',
                        margin: '32 0 0 0',
                        itemId: 'device-command-add-form'
                    },
                    {
                        itemId: 'device-command-add-property-header',
                        margins: '16 0 0 0'
                    },
                    {
                        itemId: 'device-command-add-property-form',
                        xtype: 'property-form',
                        margins: '16 0 0 0'
                    }
                ],
                buttons: [
                    {
                        text: Uni.I18n.translate('general.add','MDC','Add'),
                        ui: 'action',
                        margins: '0 0 0 164',
                        action: 'add',
                        mRID: me.device.get('mRID')
                    },
                    {
                        text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                        ui: 'link',
                        action: 'cancel',
                        mRID: me.device.get('mRID')
                    }
                ]
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        toggleId:'deviceCommands',
                        device: me.device
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});



