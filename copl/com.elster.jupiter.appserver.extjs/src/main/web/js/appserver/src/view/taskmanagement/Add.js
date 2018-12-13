/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.task-management-add',
    requires: [
        'Apr.store.CustomTaskTypes',
        'Uni.util.FormErrorMessage'
    ],

    edit: false,
    returnLink: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('taskManagement.addTask', 'APR', 'Add task'),
                itemId: 'frm-add-task',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 565
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.type', 'APR', 'Type'),
                        emptyText: Uni.I18n.translate('general.selectType', 'APR', 'Select a task type ...'),
                        required: true,
                        name: 'taskType',
                        width: 600,
                        itemId: 'task-management-task-type',
                        allowBlank: false,
                        //store: 'Apr.store.CustomTaskTypes',
                        store: me.storeTypes,
                        editable: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id'
                    },
                    {
                        xtype: 'container',
                        itemId: 'task-management-attributes'
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
                                disabled: true,
                                text: me.edit
                                    ? Uni.I18n.translate('general.save', 'APR', 'Save')
                                    : Uni.I18n.translate('general.add', 'APR', 'Add'),
                                action: me.edit
                                    ? 'editTask'
                                    : 'addTask'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'APR', 'Cancel'),
                                ui: 'link',
                                href: me.addReturnLink
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
