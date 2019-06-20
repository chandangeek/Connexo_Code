/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.view.QueueAndPriorityWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.queue-priority-window',
    itemId: 'queue-priority-window',
    requires: [
        'Uni.property.view.DefaultButton',
    ],
    modal: true,
    title: Uni.I18n.translate('general.setQueueAndPriority', 'SCT', 'Set queue and priority'),

    record: null,
    store: null,

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
            items: [{
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'label',
                    itemId: 'error-label',
                    hidden: true,
                    margin: '10 0 10 20'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.queue', 'SCT', 'Queue'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'combobox',
                            itemId: 'queue-field',
                            name: 'destination',
                            required: true,
                            editable: false,
                            store: me.store,
                            valueField: 'name',
                            displayField: 'name',
                            queryMode: 'local',
                            value: me.record.get('destination'),
                            emptyText: Uni.I18n.translate('general.selectQueue', 'SCT', 'Select a queue...')
                        },
                        {
                            xtype: 'uni-default-button',
                            itemId: 'queue-field-reset',
                            margin: '0 0 0 20',
                            handler: function() {
                                var record = me.store.findRecord('isDefault', true);

                                if (record) {
                                    me.down('[name=destination]').setValue(record.get('name'));
                                } else {
                                    me.down('[name=destination]').reset();
                                }
                            },
                            tooltip: Uni.I18n.translate('general.resetToDefault', 'SCT', 'Reset to default'),
                            hidden: false,
                            scope: me
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.priority', 'SCT', 'Priority'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'numberfield',
                            itemId: 'priority-field',
                            name: 'priority'
                        },
                        {
                            xtype: 'uni-default-button',
                            itemId: 'queue-field-reset',
                            margin: '0 0 0 20',
                            handler: function() {
                                me.down('[name=priority]').setValue(0);
                            },
                            tooltip: Uni.I18n.translate('general.resetToDefault', 'SCT', 'Reset to default'),
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
                            text: Uni.I18n.translate('general.save', 'SCT', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'SCT', 'Cancel'),
                            ui: 'link',
                            handler: me.close,
                            scope: me
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
        me.down('#queue-priority-form').loadRecord(me.record);
    }
});