Ext.define('Dlc.devicelifecycles.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycles-add',
    router: null,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.addDeviceLifeCycle', 'DLC', 'Add device life cycle'),
                itemId: 'device-life-cycles-add-form',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        text: Uni.I18n.translate('deviceLifeCycles.add.templateMsg', 'DLC', 'The new device life cycle is based on the standard template and will use the same states and transitions.'),
                        margin: '0 0 20 20',
                        width: 800
                    },
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'form-errors',
                        name: 'form-errors',
                        margin: '0 0 20 20',
                        hidden: true,
                        width: 800
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'device-life-cycle-name',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-button',
                                text: Uni.I18n.translate('general.add', 'DLC', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'DLC', 'Cancel'),
                                ui: 'link',
                                href: me.router.getRoute('administration/devicelifecycles').buildUrl()
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
