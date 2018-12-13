/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.QueuePreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.queue-preview-form',
    router: null,
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250,
        width: 1000
    },
    items: [
        {
            xtype: 'form',
            itemId: 'queue-message-preview-form',
            defaults: {
                xtype: 'container',
                layout: 'form'
            },
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
                                labelWidth: 150
                            },
                            items: [

                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                                    name: 'name',
                                    itemId: 'queue-preview-name'
                                },
                                {
                                    xtype: 'container',
                                    itemId: 'queue-properties1',
                                    layout: {
                                        type: 'vbox',
                                        align: 'stretch'
                                    },
                                    defaults: {
                                        labelWidth: 150
                                    },
                                    items: [
                                        {
                                            xtype: 'displayfield',
                                            fieldLabel: Uni.I18n.translate('general.type', 'APR', 'Type'),
                                            name: 'queueTypeName',
                                            itemId: 'queue-type'

                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            columnWidth: 0.5,
                            itemId: 'queue-properties2',
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            defaults: {
                                labelWidth: 150
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    htmlEncode: false,
                                    fieldLabel: Uni.I18n.translate('general.tasks', 'APR', 'Tasks'),
                                    name: 'queueTasks',
                                    itemId: 'queue-tasks'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {
        afterrender: function (form) {
            form.loadRecord(form.getRecord());
        }
    }

});
