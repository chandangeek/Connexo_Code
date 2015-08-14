Ext.define('Mdc.view.setup.generalattributes.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.generalAttributesSetup',
    itemId: 'generalAttributesSetup',

    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Uni.property.form.Property'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'container',
                            flex: 1
                        },
                        {
                            xtype: 'container',
                            itemId: 'editBtnContainer'
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    isEdit: false,

                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.router.arguments.deviceTypeId,
                        deviceConfigurationId: me.router.arguments.deviceConfigurationId
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.down('#editBtnContainer').add({
            xtype: 'button',
            itemId: 'generalAttributesShowEdit',
            privileges: Mdc.privileges.DeviceType.admin,
            href: typeof me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/generalattributes/edit') !== 'undefined'
                ? me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/generalattributes/edit').buildUrl() : null,
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit')
        });


    }
});