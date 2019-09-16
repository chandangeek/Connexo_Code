/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.CloseIssueForm', {

    extend: 'Uni.property.view.property.Base',

    configUrl: [
        {
            id: 'CloseIssueForm',
            issueStatusUrl: '/api/isu/statuses',
            commentPrivileges: true,
            checkboxLabel: 'Close issue'
        }
    ],

    // labelWidth: 250,

    // controlsWidth: 600,

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
                        itemId: 'cif-close-issue-checkbox',
                        name: 'checkbox',
                        labelWidth: 260,
                        cls: 'check',
                        readOnly: false,
                        msgTarget: 'under'
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'cif-issue-close-status-combo',
                        name: 'issueStatusId',
                        fieldLabel: Uni.I18n.translate('closeIssueForm.issueCloseStatus', 'UNI', 'Close status'),
                        emptyText: Uni.I18n.translate('closeIssueForm.issueCloseStatus.emptyText', 'UNI', 'Select a value'),
                        store: me.getIssueStatusStore(),
                        queryMode: 'local',
                        valueField: 'id',
                        displayField: 'name',
                        disabled: true,
                        allowBlank: false,
                        forceSelection: true,
                        labelWidth: 260,
                        width: 595
                    },
                    {
                        itemId: 'cif-comment-textarea',
                        xtype: 'textareafield',
                        name: 'comment',
                        labelWidth: 260,
                        width: 595,
                        disabled: true,
                        privileges: true,
                        fieldLabel: Uni.I18n.translate('close.comment', 'UNI', 'Comment'),
                        emptyText: Uni.I18n.translate('close.provideComment', 'UNI', 'Provide a comment (optionally)'),
                        height: 160
                    },
                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;
        me.getCloseIssueCheckbox().on('change', function () {
            me.closeIssueCheckboxChangeAction();
        });
        me.callParent(arguments);
    },

    closeIssueCheckboxChangeAction: function () {
        var me = this,
            assignIssueCheckbox = me.getCloseIssueCheckbox(),
            closeIssueStatusCombobox = me.getIssueStatusCombobox(),
            commentTextarea = me.getCommentTextarea();

        if (assignIssueCheckbox.getValue()) {
            closeIssueStatusCombobox.enable();
            commentTextarea.enable();
        } else {
            closeIssueStatusCombobox.disable();
            commentTextarea.disable();
        }
    },

    setLocalizedName: function (name) {
    },

    getField: function () {
        return this;
    },

    getCloseIssueCheckbox: function () {
        return this.down("#cif-close-issue-checkbox");
    },

    getIssueStatusCombobox: function () {
        return this.down("#cif-issue-close-status-combo");
    },

    getCommentTextarea: function () {
        return this.down("#cif-comment-textarea");
    },

    getValue: function () {
        var me = this,
            closeIssueCheckbox = me.getCloseIssueCheckbox(),
            issueStatusCombobox = me.getIssueStatusCombobox(),
            commentTextarea = me.getCommentTextarea();

        return JSON.stringify({
            checkbox: closeIssueCheckbox.getValue(),
            issueStatusId: issueStatusCombobox.getValue(),
            comment: commentTextarea.getValue()
        });
    },

    setValue: function (value) {
        var me = this,
            jsonValue = JSON.parse(value),
            closeIssueCheckbox = me.getCloseIssueCheckbox(),
            issueStatusCombobox = me.getIssueStatusCombobox(),
            commentTextarea = me.getCommentTextarea();

        closeIssueCheckbox.setValue(jsonValue.checkbox);
        issueStatusCombobox.setValue(jsonValue.issueStatusId);
        commentTextarea.setValue(jsonValue.comment);
    },

    getConfig: function () {
        var me = this,
            key = me.getProperty().get('key');

        var config = me.configUrl.filter(function (url) {
            return url.id === key;
        })[0];

        return config;
    },

    getIssueStatusStore: function () {
        var me = this,
            key = me.getProperty().get('key'),
            config = me.getConfig();

        return Ext.create('Ext.data.Store', {
            fields: ['id', 'name', 'allowForClosing'],
            autoLoad: true,
            filters: [
                function (issueStatus) {
                    return issueStatus.data.allowForClosing === true;
                }
            ],
            listeners: {
                load: function (store, records, success, operation) {
                    if (success === true) {
                        me.getIssueStatusCombobox().setValue(this.first().data.id);
                    }
                }
            },
            proxy: {
                type: 'rest',
                url: config.issueStatusUrl,
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

        if (me.getProperty().get('key') === 'CloseAlarmForm') {
            me.configUrl = [
                {
                    id: 'CloseAlarmForm',
                    issueStatusUrl: '/api/isu/statuses',
                    checkboxLabel: 'Close alarm',
                    commentPrivileges: function () {
                        return Uni.Auth.checkPrivileges(['privilege.comment.alarm']);
                    }
                }
            ];
        }
    }

});