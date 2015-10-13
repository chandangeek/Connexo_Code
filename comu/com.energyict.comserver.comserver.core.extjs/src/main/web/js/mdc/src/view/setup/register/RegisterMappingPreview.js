Ext.define('Mdc.view.setup.register.RegisterMappingPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.registerMappingPreview',
    itemId: 'registerMappingPreview',

    requires: [
        'Mdc.model.RegisterType',
        'Mdc.view.setup.register.RegisterMappingActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.CustomAttributeSetDisplay'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details','MDC','Details'),

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            privileges: Mdc.privileges.DeviceType.admin,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'register-mapping-action-menu'
            }
        }
    ],

    deviceTypeId: null,

    initComponent: function () {
        var me = this;

        this.items = [
            {
                xtype: 'panel',
                border: false,
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>' + Uni.I18n.translate('registerMapping.noRegisterMappingSelected', 'MDC', 'No register type selected') + '</h4>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        height: '100px',
                        html: '<h5>' + Uni.I18n.translate('registerMapping.selectRegisterMapping', 'MDC', 'Select a register type to see its details') + '</h5>'
                    }
                ]
            },

            {
                xtype: 'form',
                border: false,
                itemId: 'registerMappingPreviewForm',
                layout: 'column',
                defaults: {
                    columnWidth: 0.5,
                    layout: 'form'
                },
                items: [
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'reading-type-displayfield',
                                name: 'readingType'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('deviceloadprofiles.customattributeset', 'MDC', 'Custom attribute set'),
                                itemId: 'custom-attribute-set-displayfield-id',
                                xtype: 'custom-attribute-set-displayfield',
                                name: 'customPropertySet'
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'obis-displayfield',
                                name: 'obisCode'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
