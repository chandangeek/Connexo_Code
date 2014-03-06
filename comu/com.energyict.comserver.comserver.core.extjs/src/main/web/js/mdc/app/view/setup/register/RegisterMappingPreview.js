Ext.define('Mdc.view.setup.register.RegisterMappingPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.registerMappingPreview',
    itemId: 'registerMappingPreview',
    requires: [
        'Mdc.model.RegisterType'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    deviceTypeId: null,
    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                border: false,
                padding: '0 10 0 10',
                tbar: [
                    {
                        xtype: 'component',
                        html: '<H4>' + Uni.I18n.translate('registerMapping.noRegisterMappingSelected', 'MDC', 'No register type selected') + '</H4>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        height: '100px',
                        html: '<H5>' + Uni.I18n.translate('registerMapping.selectRegisterMapping', 'MDC', 'Select a register type to see its details') + '</H5>'
                    }
                ]

            },


            {
                xtype: 'form',
                border: false,
                itemId: 'registerMappingPreviewForm',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>' + Uni.I18n.translate('registerMapping.previewTitle', 'MDC', 'Selected register preview') + '</h4>',
                        itemId: 'registerMappingPreviewTitle'
                    },
                    '->',
                    {
                        icon: 'resources/images/gear-16x16.png',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        menu: {
                            items: [
                                {
                                    text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                                    itemId: 'removeRegisterMapping',
                                    action: 'removeRegisterMapping'

                                }
                            ]
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                            //                        align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('registerMapping.name', 'MDC', 'Name'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.5,
                                        fieldLabel: Uni.I18n.translate('registerMapping.readingType', 'MDC', 'Reading type'),
                                        labelAlign: 'right',
                                        labelWidth: 150,
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                name: 'mrid'
                                            },
                                            {
                                                xtype: 'component',
                                                html: '&nbsp;&nbsp;'
                                            },
                                            {
                                                xtype: 'button',
                                                icon: 'resources/images/information.png',
                                                tooltip: 'Reading type info',
                                                cls: 'uni-btn-transparent',
                                                handler: function (item, test) {
                                                    var record = me.down('#registerMappingPreviewForm').form.getRecord();
                                                    this.fireEvent('showReadingTypeInfo', record);
                                                },
                                                itemId: 'raadingTypeBtn',
                                                action: 'showReadingTypeInfo'
                                            }

                                        ]
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'obisCode',
                                        fieldLabel: Uni.I18n.translate('registerMapping.obisCode', 'MDC', 'OBIS code'),
                                        labelAlign: 'right',
                                        labelWidth: 150
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
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'measurementKind',
                                        fieldLabel: Uni.I18n.translate('registerMapping.type', 'MDC', 'Type'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'dataCollectionGroup',
                                        fieldLabel: Uni.I18n.translate('registerMapping.dataCollectionGroup', 'MDC', 'Data collection group'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    }

                                ]
                            }
                        ]
                    }
                    /*,
                     {
                     xtype: 'toolbar',
                     docked: 'bottom',
                     title: 'Bottom Toolbar',
                     items: [
                     '->',
                     {
                     xtype: 'component',
                     itemId: 'registerMappingDetailsLink',
                     html: '' // filled in in Controller
                     }

                     ]
                     }*/
                ]
            }
        ]
        this.callParent(arguments);

    }
})
;

