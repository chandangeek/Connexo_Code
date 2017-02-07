/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.AssigneeForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.assignee-form',
    requires: [
        'Bpm.view.task.UserAssigneeCombo'
    ],

    workgroup: {
        dataIndex: 'workgroup',
        name: 'workgroup',
        valueField: 'id',
        displayField: 'name',
        store: 'Bpm.store.task.TaskWorkgroupAssignees'
    },
    user: {
        dataIndex: 'actualOwner',
        name: 'assignee',
        valueField: 'id',
        displayField: 'name',
        store: 'Bpm.store.task.TasksFilterAllUsers'
    },
    allUsersUrl: '',
    workgroupUsersUrl: '',
    withCheckBox: false,
    itemId: 'aaa',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                width: '100%',
                margin: '0 0 10 0',
                defaults: me.defaults,
                items: [
                    {
                        xtype: 'checkbox',
                        labelWidth: 10,
                        width: 15,
                        itemId: 'workgroup-assignee-check',
                        name: 'cbWorkgroupAssignee',
                        inputValue: 'workgroupAssign',
                        hidden: !me.withCheckBox,
                        listeners: {
                            change: function () {
                                var me = this,
                                    comboIdentifier = me.up('form').down('combo[itemId=cbo-workgroup-assignee]');

                                if (me.isHidden())
                                    return;

                                if (me.getValue())
                                    me.up('form').down(comboIdentifier).enable();
                                else
                                    me.up('form').down(comboIdentifier).disable();
                                me.up('form').down(comboIdentifier).fireEvent('workgroupEnableChanged');
                            },
                            render: function (combo) {
                                this.fireEvent('change', this);
                            }
                        }
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-workgroup-assignee',
                        fieldLabel: Uni.I18n.translate('general.workgroup', 'BPM', 'Workgroup'),
                        queryMode: 'local',
                        allowBlank: false,
                        emptyText: Uni.I18n.translate('task.startTypingForWorkgroup', 'BPM', 'Start typing for workgroup'),
                        msgTarget: 'under',
                        editable: false,
                        dataIndex: me.workgroup.dataIndex,
                        name: me.workgroup.name,
                        valueField: me.workgroup.valueField,
                        displayField: me.workgroup.displayField,
                        store: me.workgroup.store,
                        value: me.workgroup.value,
                        listeners: {
                            render: function () {
                                this.store.load();
                            },
                            change: function (combo, newValue, oldValue) {
                                if (oldValue == undefined) {
                                    this.up('#frm-assignee-user').down('#cbo-user-assignee').workgroupId = newValue;
                                    this.up('#frm-assignee-user').down('#cbo-user-assignee').fireEvent('workgroupFirstChanged', newValue);
                                }
                            },
                            select: function (combo, newValue) {
                                this.up('#frm-assignee-user').down('#cbo-user-assignee').fireEvent('workgroupChanged', newValue[0].get('id'));
                            },
                            workgroupEnableChanged: function (combo) {
                                var me = this,
                                    checkboxIdentifier = me.up('form').down('checkbox[itemId=workgroup-assignee-check]'),
                                    enable = me.up('form').down(checkboxIdentifier).getValue();
                                this.up('#frm-assignee-user').down('#cbo-user-assignee').fireEvent('workgroupEnableChanged', enable);
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: me.defaults,
                width: '100%',
                items: [
                    {
                        xtype: 'checkbox',
                        labelWidth: 10,
                        width: 15,
                        itemId: 'user-assignee-check',
                        name: 'cbUserAssignee',
                        inputValue: 'userAssign',
                        hidden: !me.withCheckBox,
                        listeners: {
                            change: function () {
                                var me = this,
                                    comboIdentifier = me.up('form').down('combo[itemId=cbo-user-assignee]');

                                if (me.isHidden())
                                    return;
                                if (me.getValue())
                                    me.up('form').down(comboIdentifier).enable();
                                else
                                    me.up('form').down(comboIdentifier).disable();
                            },
                            render: function (combo) {
                                this.fireEvent('change', this);
                            }
                        }
                    },
                    {
                        xtype: 'user-assignee-combo',
                        itemId: 'cbo-user-assignee',
                        fieldLabel: Uni.I18n.translate('general.user', 'BPM', 'User'),
                        queryMode: 'local',
                        allowBlank: false,
                        editable: false,
                        emptyText: Uni.I18n.translate('task.startTypingForUsers', 'BPM', 'Start typing for users'),
                        msgTarget: 'under',
                        dataIndex: me.user.dataIndex,
                        name: me.user.name,
                        valueField: me.user.valueField,
                        displayField: me.user.displayField,
                        store: me.user.store,
                        allUsersUrl: me.allUsersUrl,
                        workgroupUsersUrl: me.workgroupUsersUrl,
                        value: Uni.I18n.translate('bpm.task.user.unassigned', 'BPM', 'Unassigned')
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});
