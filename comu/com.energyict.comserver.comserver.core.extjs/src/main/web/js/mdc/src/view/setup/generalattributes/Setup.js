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
            title: Uni.I18n.translate('deviceconfigurationmenu.generalAttributes', 'MDC', 'General attributes'),
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'property-form',
                    isEdit: false,
                    layout: 'column',

                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250,
                        columnWidth: 0.5
                    },
                    flex: 1
                },
                {
                    xtype: 'container',
                    itemId: 'editBtnContainer'
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
                        toggle: 10,
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
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
            href: typeof me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/generalattributes/edit') !== 'undefined'
                ? me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/generalattributes/edit').buildUrl() : null,
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit')
        });


    }
});