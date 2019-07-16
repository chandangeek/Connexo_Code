/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.AssignIssueForm', {

    extend: 'Uni.property.view.property.Base',

    requires: [
        'Uni.property.view.property.UserAssigneeCombo'
    ],

    configUrl: [
        {
            id: 'AssignIssueForm',
            workgroupUrl: '/api/isu/workgroups',
            userUrl: '/api/isu/assignees/users',
            workroupUsersUrl: '/api/isu/workgroups/{0}/users',
            commentPrivileges: true,
            checkboxLabel: 'Assign issue'
        }
    ],

    labelWidth: 250,

    controlsWidth: 600,

    getEditCmp: function () {
        var me = this;

        me.resolveConfig();
        me.layout = 'vbox';
        return [
            {
                items: [
                    {
                        xtype: 'checkbox',
                        fieldLabel: me.getConfig().checkboxLabel,
                        itemId: 'aif-assign-issue-checkbox',
                        name: 'checkbox',
                        labelWidth: 260,
                        cls: 'check',
                        readOnly: false,
                        msgTarget: 'under'
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'aif-workgroup-assignee-combo',
                        fieldLabel: Uni.I18n.translate('assign.workgroup', 'UNI', 'Workgroup'),
                        queryMode: 'local',
                        name: 'workgroupId',
                        labelWidth: 260,
                        width: 595,
                        valueField: 'id',
                        displayField: 'name',
                        allowBlank: false,
                        store: me.getWorkgroupStore(),
                        emptyText: Uni.I18n.translate('assign.startTypingForWorkgroup', 'UNI', 'Start typing for workgroup'),
                        msgTarget: 'under',
                        editable: false,
                        disabled: true,
                        listeners: {
                            render: function () {
                                this.store.load();
                            },
                            select: function (combo, newValue) {
                                this.ownerCt.down('#aif-user-assignee-combo').fireEvent('workgroupChanged', newValue[0].get('id'));
                            }
                        }
                    },
                    {
                        xtype: 'usr-assignee-combo',
                        itemId: 'aif-user-assignee-combo',
                        fieldLabel: Uni.I18n.translate('assign.user', 'UNI', 'User'),
                        queryMode: 'local',
                        name: 'userId',
                        labelWidth: 260,
                        width: 595,
                        valueField: 'id',
                        displayField: 'name',
                        allowBlank: false,
                        editable: false,
                        disabled: true,
                        configUrl: me.getConfig(),
                        store: me.getUserStore(),
                        emptyText: Uni.I18n.translate('assign.startTypingForUsers', 'UNI', 'Start typing for users'),
                        msgTarget: 'under'
                    },
                    {
                        itemId: 'aif-comment-textarea',
                        xtype: 'textareafield',
                        name: 'comment',
                        labelWidth: 260,
                        width: 595,
                        disabled: true,
                        privileges: me.getConfig().commentPrivileges,
                        fieldLabel: Uni.I18n.translate('assign.comment', 'UNI', 'Comment'),
                        emptyText: Uni.I18n.translate('assign.provideComment', 'UNI', 'Provide a comment (optionally)'),
                        height: 160
                    },
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;
        me.getAssignIssueCheckbox().on('change', function () {
            me.assignIssueCheckboxChangeAction();
        });
        me.callParent(arguments);
    },

    assignIssueCheckboxChangeAction: function () {
        var me = this,
            assignIssueCheckbox = me.getAssignIssueCheckbox(),
            workgroupCombobox = me.getWorkgroupCombobox(),
            userCombobox = me.getUserCombobox(),
            commentTextarea = me.getCommentTextarea();

        if (assignIssueCheckbox.getValue()) {
            workgroupCombobox.enable();
            userCombobox.enable();
            commentTextarea.enable();
        } else {
            workgroupCombobox.disable();
            userCombobox.disable();
            commentTextarea.disable();
        }
    },

    setLocalizedName: function (name) {
    },

    getField: function () {
        return this;
    },

    getAssignIssueCheckbox: function () {
        return this.down("#aif-assign-issue-checkbox");
    },

    getWorkgroupCombobox: function () {
        return this.down("#aif-workgroup-assignee-combo");
    },

    getUserCombobox: function () {
        return this.down("#aif-user-assignee-combo");
    },

    getCommentTextarea: function () {
        return this.down("#aif-comment-textarea");
    },

    getValue: function () {
        var me = this,
            checkbox = me.down('#aif-assign-issue-checkbox'),
            workgroupCombo = me.down('#aif-workgroup-assignee-combo'),
            userCombo = me.down('#aif-user-assignee-combo'),
            comment = me.down('#aif-comment-textarea');

        return JSON.stringify({
            checkbox: checkbox.getValue(),
            workgroupId: workgroupCombo.getValue(),
            userId: userCombo.getValue(),
            comment: comment.getValue()
        });
    },

    setValue: function (value) {
        var me = this,
            jsonValue = JSON.parse(value),
            workgroupCombo = me.down('#aif-workgroup-assignee-combo'),
            userCombo = me.down('#aif-user-assignee-combo');

        workgroupCombo.suspendEvents();
        workgroupCombo.setValue(jsonValue.workgroupId);
        workgroupCombo.resumeEvents();
        userCombo.setWorkgroupId(jsonValue.workgroupId);
        userCombo.setValue(jsonValue.userId);
    },

    getConfig: function () {
        var me = this,
            key = me.getProperty().get('key');

        var config = me.configUrl.filter(function (url) {
            return url.id === key;
        })[0];

        return config;
    },

    getWorkgroupStore: function () {
        var me = this,
            key = me.getProperty().get('key'),
            config = me.getConfig();

        return Ext.create('Ext.data.Store', {
            fields: ['id', 'name'],
            autoLoad: false,
            proxy: {
                type: 'rest',
                url: config.workgroupUrl,
                reader: {
                    type: 'json',
                    root: 'workgroups'
                },
                pageParam: false,
                startParam: false,
                limitParam: false
            }
        });
    },

    getUserStore: function () {
        var me = this,
            key = me.getProperty().get('key'),
            config = me.getConfig();

        return Ext.create('Ext.data.Store', {
            fields: ['id', 'name'],
            autoLoad: false,
            proxy: {
                type: 'rest',
                url: config.userUrl,
                reader: {
                    type: 'json',
                    root: 'data'
                },
                pageParam: false,
                startParam: false,
                limitParam: false
            }
        });
    },

    resolveConfig: function () {
        var me = this;

        if (me.getProperty().get('key') === 'AssignAlarmForm') {
            me.configUrl = [
                {
                    id: 'AssignAlarmForm',
                    workgroupUrl: '/api/dal/workgroups',
                    userUrl: '/api/dal/assignees',
                    workroupUsersUrl: '/api/dal/workgroups/{0}/users',
                    checkboxLabel: 'Assign alarm',
                    commentPrivileges: function () {
                        return Uni.Auth.checkPrivileges(['privilege.comment.alarm']);
                    }
                }
            ];
        }
    }

});