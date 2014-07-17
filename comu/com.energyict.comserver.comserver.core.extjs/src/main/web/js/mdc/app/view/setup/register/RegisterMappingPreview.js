Ext.define('Mdc.view.setup.register.RegisterMappingPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.registerMappingPreview',
    itemId: 'registerMappingPreview',

    requires: [
        'Mdc.model.RegisterType',
        'Mdc.view.setup.register.RegisterMappingActionMenu',
        'Uni.form.field.ObisDisplay'
    ],

    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: 'Details',

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
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
                                columnWidth: 0.49,
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
                                        labelWidth: 250
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.49,
                                        fieldLabel: Uni.I18n.translate('registerMapping.readingType', 'MDC', 'Reading type'),
                                        labelAlign: 'right',
                                        labelWidth: 250,
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                name: 'mrid',
                                                itemId: 'preview_mrid'
                                            },
                                            {
                                                xtype: 'component',
                                                html: '&nbsp;&nbsp;'
                                            },
                                            {
                                                xtype: 'button',
                                                icon: '../ext/packages/uni-theme-skyline/resources/images/icon-info-small.png',
                                                tooltip: 'Reading type info',
                                                cls: 'uni-btn-transparent',
                                                handler: function (item, test) {
                                                    var record = me.down('#registerMappingPreviewForm').form.getRecord();
                                                    this.fireEvent('showReadingTypeInfo', record);
                                                },
                                                itemId: 'readingTypeBtn',
                                                action: 'showReadingTypeInfo'
                                            }

                                        ]
                                    },
                                    {
                                        xtype: 'obis-displayfield',
                                        name: 'obisCode',
                                        labelAlign: 'right',
                                        labelWidth: 250
                                    }
                                ]
                            },
                            {
                                xtype: 'container',
                                columnWidth: 0.49,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                items: [
                                    /* {
                                     xtype: 'displayfield',
                                     name: 'dataCollectionGroup',
                                     fieldLabel: Uni.I18n.translate('registerMapping.dataCollectionGroup', 'MDC', 'Data collection group'),
                                     labelAlign: 'right',
                                     labelWidth: 250
                                     }*/
                                    {
                                        xtype: 'displayfield',
                                        name: 'unit',
                                        fieldLabel: Uni.I18n.translate('registerMapping.measurementUnit', 'MDC', 'Unit of measure'),
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'timeOfUse',
                                        fieldLabel: Uni.I18n.translate('registerMapping.timeOfUse', 'MDC', 'Time of use'),
                                        labelWidth: 150
                                    }

                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
