Ext.define('Mdc.view.setup.registergroup.RegisterGroupPreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    //margins: '0 10 10 10',
    alias: 'widget.registerGroupPreview',
    itemId: 'registerGroupPreview',
    requires: [
        'Mdc.model.RegisterGroup'
    ],
    padding: '20 0 0 0',
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
                //padding: '0 10 0 10',
                tbar: [
                    {
                        xtype: 'component',
                        html: '<b>' + Uni.I18n.translate('registerGroup.noRegisterGroupSelected', 'MDC', 'No register group selected') + '</b>'
                    }
                ],
                items: [
                    {
                        xtype: 'component',
                        height: '100px',
                        html: '<b>' + Uni.I18n.translate('registerGroup.selectRegisterGroup', 'MDC', 'Select a register group to see its details') + '</b>'
                    }
                ]

            },
            {
                xtype: 'form',
                border: false,
                itemId: 'registerGroupPreviewForm',
                //padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                tbar: [
                    {
                        xtype: 'component',
                        html: '<b>' + Uni.I18n.translate('registerGroup.previewTitle', 'MDC', 'Selected register group preview') + '</b>',
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
                        xtype: 'emptygridcontainer',
                        itemId: 'registerTypeEmptyGrid',
                        //padding: '10 10 10 10',
                        grid: {
                            xtype: 'registerTypeGrid',
                            withPaging: false,
                            withActions: false,
                            maxHeight: 500
                        },
                        emptyComponent: {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'left'
                            },
                            minHeight: 20,
                            items: [
                                {
                                    xtype: 'image',
                                    margin: '0 10 0 0',
                                    src: "../mdc/resources/images/information.png",
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: '<b>'+ Uni.I18n.translate('registerGroupPreview.empty.title', 'MDC', 'No register types found') +'</b><br>' +
                                                Uni.I18n.translate('registerGroupPreview.empty.detail', 'MDC', 'There are no register types. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('registerGroupPreview.empty.list.item1', 'MDC', 'No register types are associated to this register group.') + '</li></lv><br>' +
                                                Uni.I18n.translate('registerGroupPreview.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            xtype: 'button',
                                            itemId: 'editEmptyPreviewButton',
                                            margin: '10 0 0 0',
                                            text: Uni.I18n.translate('registerGroup.edit', 'MDC', 'Edit register group'),
                                            action: 'editRegisterGroup'
                                        }
                                    ]
                                }
                            ]
                        }
                    },
                    {
                        xtype: 'registerTypePreview',
                        withActions: false
                    }
                ]
            }
        ]
        this.callParent(arguments);

    }
})
;

