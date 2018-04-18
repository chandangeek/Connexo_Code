/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.SetQueue', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.set-queue',
    columns: 1,
    defaults: {
        name: 'setQueue'
    },
    requires: [
        'Apr.store.TasksType',
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                width: 500,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'task-type',
                        name: 'taskType',
                        fieldLabel: Uni.I18n.translate('general.selectqueue', 'APR', 'Select queue'),
                        labelWidth: 150,
                        required: true,
                        store: 'Apr.store.TasksType',
                        editable: false,
                        disable: false,
                        emptyText: Uni.I18n.translate('general.selectqueue', 'APR', 'Select queue'),
                        allowBlank: false,
                        displayField: 'queue',
                        valueField: 'queue',
                        width: 400,
                        listeners: {
                            render: {
                                fn: function () {
                                    var mee = this,
                                        taskStore = mee.getStore();

                                    taskStore.getProxy().setUrl(me.record.getId());
                                    taskStore.load();

                                }
                            }
                        }
                    }

                ],
                action: 'applyAction'
            }
        ];

        me.callParent(arguments);
    }
});