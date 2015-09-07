Ext.define('Mdc.view.setup.registertype.RegisterTypeDetail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeDetail',
    itemId: 'registerTypeDetail',
    requires: [
        'Mdc.view.setup.registertype.RegisterTypeGrid',
        'Mdc.view.setup.registertype.RegisterTypePreview',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
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
                    '->',
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        privileges: Mdc.privileges.MasterData.admin,
                        iconCls: 'x-uni-action-iconD',
                        menu: {
                            xtype: 'register-type-action-menu'
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
                                    /*   {
                                     xtype: 'displayfield',
                                     name: 'dataCollectionGroup',
                                     fieldLabel: Uni.I18n.translate('registerType.dataCollectionGroup', 'MDC', 'Data collection group'),
                                     labelAlign: 'right',
                                     labelWidth: 150
                                     }*/
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


