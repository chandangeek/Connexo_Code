Ext.define('Mdc.view.setup.registergroup.RegisterGroupPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    margins: '0 10 10 10',
    alias: 'widget.registerGroupPreview',
    itemId: 'registerGroupPreview',
    requires: [
        'Mdc.model.RegisterGroup'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

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
                        html: '<H4>' + Uni.I18n.translate('registerGroup.noRegisterGroupSelected', 'MDC', 'No register group selected') + '</H4>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        height: '100px',
                        html: '<H5>' + Uni.I18n.translate('registerGroup.selectRegisterGroup', 'MDC', 'Select a register group to see its details') + '</H5>'
                    }
                ]

            },
            {
                xtype: 'form',
                border: false,
                itemId: 'registerGroupPreviewForm',
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                tbar: [
                    {
                        xtype: 'component',
                        html: '<h4>' + Uni.I18n.translate('registerGroup.previewTitle', 'MDC', 'Selected register group preview') + '</h4>',
                        itemId: 'registerGroupPreviewTitle'
                    },
                    '->',
                    {
                        icon: '../mdc/resources/images/gear-16x16.png',
                        text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                        menu: {
                            items: [
                                {
                                    text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                    itemId: 'editRegisterGroup',
                                    action: 'editRegisterGroup'
                                }
                                /*{
                                    xtype: 'menuseparator'
                                },
                                {
                                    text: Uni.I18n.translate('general.delete', 'MDC', 'Delete'),
                                    itemId: 'deleteRegisterType',
                                    action: 'deleteRegisterType'

                                }*/
                            ]
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'registerTypeGrid',
                        itemId: 'registerGroupPreviewGrid'
                    },
                    {
                        xtype: 'registerTypePreview',
                        itemId: 'registerGroupPreviewDetails'
                    }
                ]
            }
        ]
        this.callParent(arguments);

    }
})
;

