Ext.define('Mdc.view.setup.register.RegisterMappingPreview', {
    extend: 'Ext.panel.Panel',
    border: false,
    margins: '0 10 10 10',
    alias: 'widget.registerMappingPreview',
    itemId: 'registerMappingPreview',
    hidden: true,
    requires: [
        'Mdc.model.RegisterMapping'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    tbar: [
        {
            xtype: 'component',
            html: '<h4>Selected register preview</h4>',
            itemId: 'registerMappingPreviewTitle'
        },
        '->',
        {
            icon: 'resources/images/gear-16x16.png',
            text: 'Actions',
            menu: {
                items: [
                    {
                        text: 'Clone',
                        itemId: 'cloneRegisterMapping',
                        action: 'cloneRegisterMapping'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: 'Delete',
                        itemId: 'deleteRegisterMapping',
                        action: 'deleteRegisterMapping'

                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        text: 'Edit',
                        itemId: 'editRegisterMapping',
                        action: 'editRegisterMapping'

                    }
                ]
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceTypePreviewForm',
            padding: '10 10 0 10',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },

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
                                    fieldLabel: 'Name:',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'obisCode',
                                    fieldLabel: 'ObisCode:',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'unit',
                                    fieldLabel: 'unit',
                                    labelAlign: 'right',
                                    labelWidth: 150
                                }
                            ]
                        }
                    ]
                },
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
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

