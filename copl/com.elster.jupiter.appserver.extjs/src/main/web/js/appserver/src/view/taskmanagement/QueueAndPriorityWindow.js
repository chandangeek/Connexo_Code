/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.QueueAndPriorityWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.queue-priority-window-management',
    itemId: 'queue-priority-window-management',
    requires: [
        'Uni.property.view.DefaultButton',
    ],
    modal: true,
    title: Uni.I18n.translate('general.setQueueAndPriority', 'APR', 'Set queue and priority'),
    record: null,
    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            itemId: 'queue-priority-form',
            padding: 0,
            defaults: {
                width: 418,
                labelWidth: 150
            },
            margin: '20 0 0 0',
            items: [
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'combobox',
                            itemId: 'queue-field',
                            name: 'queue',
                            required: true,
                            editable: false,
                            store: me.store,
                            valueField: 'queue',
                            displayField: 'queue',
                            queryMode: 'local',
                            value: me.record.get('queue'),
                            emptyText: Uni.I18n.translate('general.selectQueue', 'APR', 'Select queue.'),
                        },
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.priority', 'APR', 'Priority'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'numberfield',
                            itemId: 'priority-field',
                            name: 'priority',
                            value: me.record.get('priority'),
                            listeners: {
                                change: function(field, val) {
                                    me.down('#priority-field-reset').setDisabled(Number(val) === 0);
                                },
                                scope: me,
                            }
                        },
                        {
                            xtype: 'uni-default-button',
                            itemId: 'priority-field-reset',
                            margin: '0 0 0 20',
                            handler: function() {
                                me.down('[name=priority]').setValue(0);
                            },
                            tooltip: Uni.I18n.translate('general.resetToDefault', 'APR', 'Reset to default'),
                            disabled: true,
                            hidden: false,
                            scope: me
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [{
                            xtype: 'button',
                            itemId: 'save-queue-priority-button',
                            text: Uni.I18n.translate('general.save', 'APR', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'APR', 'Cancel'),
                            ui: 'link',
                            handler: me.close,
                            scope: me
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});