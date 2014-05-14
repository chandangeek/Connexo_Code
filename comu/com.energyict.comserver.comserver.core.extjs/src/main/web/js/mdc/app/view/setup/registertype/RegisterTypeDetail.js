Ext.define('Mdc.view.setup.registertype.RegisterTypeDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeDetail',
    itemId: 'registerTypeDetail',
    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'form',
                border: false,
                itemId: 'registerTypeDetailForm',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                tbar: [
                    {
                        xtype: 'component',
                        html: '<b>' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</b>',
                        itemId: 'registerTypePreviewTitle'
                    },
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    '->',
                    {
                        icon: '../mdc/resources/images/gear-16x16.png',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        menu: {
                            items: [
                                {
                                    text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                    itemId: 'editRegisterType',
                                    action: 'editRegisterType'

                                },
                                {
                                    xtype: 'menuseparator'
                                },
                                {
                                    text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                    itemId: 'deleteRegisterType',
                                    action: 'deleteRegisterType'

                                }
                            ]
                        }
                    }
                ],

                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column',
                            align: 'stretch'
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
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('registerType.name', 'MDC', 'Name'),
                                        labelAlign: 'right',
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.5,
                                        fieldLabel: Uni.I18n.translate('registerType.readingType', 'MDC', 'Reading type'),
                                        labelAlign: 'right',
                                        labelWidth: 150,
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        items: [
                                            {
                                                xtype: 'displayfield',
                                                name: 'mrid',
                                                itemId: 'detail_mrid'
                                            },
                                            {
                                                xtype: 'component',
                                                html: '&nbsp;&nbsp;'
                                            },
                                            {
                                                xtype: 'button',
                                                icon: '../mdc/resources/images/information.png',
                                                tooltip: 'Reading type info',
                                                cls: 'uni-btn-transparent',
                                                handler: function (item, test) {
                                                    var record = me.down('#registerTypeDetailForm').form.getRecord();
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
                                        fieldLabel: Uni.I18n.translate('registerType.obisCode', 'MDC', 'OBIS code'),
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
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'dataCollectionGroup',
                                        fieldLabel: Uni.I18n.translate('registerType.dataCollectionGroup', 'MDC', 'Data collection group'),
                                        labelAlign: 'right',
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


