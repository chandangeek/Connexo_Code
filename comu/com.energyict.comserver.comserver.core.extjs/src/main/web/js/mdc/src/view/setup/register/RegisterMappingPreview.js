Ext.define('Mdc.view.setup.register.RegisterMappingPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.registerMappingPreview',
    itemId: 'registerMappingPreview',

    requires: [
        'Mdc.model.RegisterType',
        'Mdc.view.setup.register.RegisterMappingActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
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
                                xtype: 'obis-displayfield',
                                name: 'obisCode'
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 150
                        },
                        items: [
                            /* {
                             name: 'dataCollectionGroup',
                             fieldLabel: Uni.I18n.translate('registerMapping.dataCollectionGroup', 'MDC', 'Data collection group'),
                             }*/
                          /*  {
                                name: 'unit',
                                fieldLabel: Uni.I18n.translate('registerMapping.measurementUnit', 'MDC', 'Unit of measure')
                            },
                            {
                                name: 'timeOfUse',
                                fieldLabel: Uni.I18n.translate('registerMapping.timeOfUse', 'MDC', 'Time of use')
                            }*/

                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
