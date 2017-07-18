/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.logbooktype.LogbookTypePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.logbookTypePreview',
    itemId: 'logbookTypePreview',
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.MasterData.admin,
            menu: {
                xtype: 'logbook-type-action-menu'
            }
        }
    ],
    requires: [
        'Mdc.model.LogbookType',
        'Mdc.view.setup.logbooktype.LogbookTypeActionMenu',
        'Uni.form.field.ObisDisplay'
    ],
    items: [
        {
            xtype: 'form',
            border: false,
            itemId: 'logbookTypePreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    columnWidth: 0.5,
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
                                    xtype: 'displayfield',
                                    itemId: 'logbookTypeDetailsName',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
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
                                    xtype: 'obis-displayfield',
                                    name: 'obisCode'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});