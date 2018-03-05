/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.AddMessageQueueForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-queue-message-form',
    requires: [
        'Apr.store.QueuesType',
    ],

    returnLink: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                title: Uni.I18n.translate('general.addQueueMessage', 'APR', 'Add queue message'),
                itemId: 'add-queue-form',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'name-queue',
                        fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                        allowBlank: false,
                        required: true,
                        width: 600
                        // listeners: {
                        //     afterrender: function(field) {
                        //         field.focus(false, 200);
                        //     }
                        // }
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'queue-type',
                        name: 'queueType',
                        fieldLabel: Uni.I18n.translate('general.type', 'APR', 'Type'),
                        labelWidth: 250,
                        required: true,
                        store: 'Apr.store.QueuesType',
                        editable: false,
                        disable: false,
                        emptyText: Uni.I18n.translate('general.selectType', 'APR', 'Select a task type ...'),
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'value',
                        width: 600
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-button',
                                ui: 'action',
                                text: Uni.I18n.translate('general.add', 'APR', 'Add'),
                                action: me.add
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'APR', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/messagequeues'
                            }
                        ]
                    }

                ]
            }
        ]
        me.callParent(arguments);
    }
});
