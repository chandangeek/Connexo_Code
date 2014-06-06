Ext.define('Mdc.view.setup.connectionmethod.ConnectionMethodPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.connectionMethodPreview',
    itemId: 'connectionMethodPreview',
    requires: [
        'Mdc.model.ConnectionMethod',
        'Mdc.view.setup.property.PropertyView',
        'Mdc.view.setup.connectionmethod.ConnectionMethodActionMenu'
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
                xtype: 'connection-method-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('connectionmethod.noConnectionMethodSelected', 'MDC', 'No connection method selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('connectionmethod.selectConnectionMethod', 'MDC', 'Select a connection method to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'connectionMethodPreviewForm',
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
                            columnWidth: 0.49,
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
                                    fieldLabel: Uni.I18n.translate('connectionmethod.name', 'MDC', 'Name'),
                                    itemId: 'deviceName'

                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'direction',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.direction', 'MDC', 'Direction')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'allowSimultaneousConnections',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.simultaneousConnectionsAllowed', 'MDC', 'Simultaneous connections allowed'),
                                    renderer: function(value){
                                        return value? Uni.I18n.translate('general.yes', 'MDC', 'Yes'):Uni.I18n.translate('general.no', 'MDC', 'No');

                                    }
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
                            defaults: {
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'connectionType',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.connectionType', 'MDC', 'Connection type')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'isDefault',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.default', 'MDC', 'Default'),
                                    renderer: function(value){
                                        return value? Uni.I18n.translate('general.yes', 'MDC', 'Yes'):Uni.I18n.translate('general.no', 'MDC', 'No');
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'comPortPool',
                                    fieldLabel: Uni.I18n.translate('connectionmethod.portPool', 'MDC', 'Port pool')
                                }
                            ]
                        }


                    ]
                },
                {
                    xtype: 'propertyView'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});


