/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registertype.RegisterTypePreview', {
    extend: 'Ext.panel.Panel',
    border: true,
    frame: true,
    alias: 'widget.registerTypePreview',
    itemId: 'registerTypePreview',
    requires: [
        'Mdc.model.RegisterType',
        'Ext.layout.container.Column',
        'Ext.form.FieldSet',
        'Mdc.view.setup.registertype.RegisterTypeActionMenu',
        'Uni.form.field.ObisDisplay',
        'Uni.form.field.ReadingTypeDisplay'
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
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 150
                                },
                                items: [
                                    {
                                        xtype: 'reading-type-displayfield',
                                        name: 'readingType'
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
        ];

        if (this.withActions) {


            this.tools = [
                {
                    xtype: 'uni-button-action',
                    privileges: Mdc.privileges.MasterData.admin,
                    menu: {
                        xtype: 'register-type-action-menu'
                    }
                }
            ]
        }

        this.callParent(arguments);
    }
});