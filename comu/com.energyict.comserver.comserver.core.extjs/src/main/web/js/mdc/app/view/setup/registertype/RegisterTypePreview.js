Ext.define('Mdc.view.setup.registertype.RegisterTypePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    frame: true,
    alias: 'widget.registerTypePreview',
    itemId: 'registerTypePreview',
    requires: [
        'Mdc.model.RegisterType',
        'Ext.layout.container.Column',
        'Ext.form.FieldSet'
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
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('registerType.name', 'MDC', 'Name'),
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'fieldcontainer',
                                        columnWidth: 0.5,
                                        fieldLabel: Uni.I18n.translate('registerType.readingType', 'MDC', 'Reading type'),
                                        labelWidth: 150,
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
                                                icon: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                                tooltip: Uni.I18n.translate('readingType.tooltip', 'MDC', 'Reading type info'),
                                                cls: 'uni-btn-transparent',
                                                handler: function (item, test) {
                                                    var record = me.down('#registerTypePreviewForm').form.getRecord();
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
                                        name: 'unit',
                                        fieldLabel: Uni.I18n.translate('registerType.measurementUnit', 'MDC', 'Unit of measure'),
                                        labelWidth: 150
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'timeOfUse',
                                        fieldLabel: Uni.I18n.translate('registerType.timeOfUse', 'MDC', 'Time of use'),
                                        labelWidth: 150
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
                    itemId: 'actionsButton',
                    // TODO Replace this icon below with an 'actions' ui.
                    icon: '../mdc/resources/images/actionsDetail.png',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    menu: {
                        items: [
                            {
                                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                itemId: 'editRegisterType',
                                action: 'editRegisterType'
                            },
                            {
                                text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                itemId: 'deleteRegisterType',
                                action: 'deleteRegisterType'

                            }
                        ]
                    }
                }
            ];
        }

        this.callParent(arguments);
    }
});