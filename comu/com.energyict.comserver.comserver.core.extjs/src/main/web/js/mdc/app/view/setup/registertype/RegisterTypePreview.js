Ext.define('Mdc.view.setup.registertype.RegisterTypePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    frame: true,
    alias: 'widget.registerTypePreview',
    itemId: 'registerTypePreview',
    requires: [
        'Mdc.model.RegisterType',
        'Ext.layout.container.Column',
        'Ext.form.FieldSet',
        'Mdc.view.setup.registertype.RegisterTypeActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    withActions: true,

    initComponent: function () {
        var me = this;

        this.items = [
            {
                xtype: 'form',
                border: false,
                itemId: 'registerTypePreviewForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 150
                                },
                                items: [
                                    {
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('registerType.name', 'MDC', 'Name')
                                    },
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
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 150
                                },
                                items: [
                                    {
                                        name: 'unitOfMeasure',
                                        fieldLabel: Uni.I18n.translate('registerType.measurementUnit', 'MDC', 'Unit of measure'),
                                        valueToRaw: function(value) {
                                            debugger;
                                            if(value != undefined){
                                                return value.localizedValue;
                                            }
                                        }
                                    },
                                    {
                                        name: 'timeOfUse',
                                        fieldLabel: Uni.I18n.translate('registerType.timeOfUse', 'MDC', 'Time of use')
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        if (this.withActions) {


            this.tools = [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                    iconCls: 'x-uni-action-iconD',
                    menu: {
                        xtype: 'register-type-action-menu'
                    }
                }
            ]
        }

        this.callParent(arguments);
    }
});