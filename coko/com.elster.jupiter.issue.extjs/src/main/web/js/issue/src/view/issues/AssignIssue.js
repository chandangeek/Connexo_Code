Ext.define('Isu.view.issues.AssignIssue', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.view.component.UserAssigneeCombo',
        'Isu.store.UserList'
    ],
    alias: 'widget.assign-issue',
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                title: Uni.I18n.translate('issue.assignIssue', 'ISU', 'Assign issue'),
                itemId: 'frm-assign-issue',
                defaults: {
                    labelWidth: 250,
                    width: 600,
                    enforceMaxLength: true
                },
                items: [
                    {
                        itemId: 'assign-issue-form-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-workgroup-issue-assignee',
                        fieldLabel: Uni.I18n.translate('general.workgroup', 'ISU', 'Workgroup'),
                        required: true,
                        queryMode: 'local',
                        name: 'workgroupId',
                        valueField: 'id',
                        displayField: 'name',
                        allowBlank: false,
                        store: 'Isu.store.IssueWorkgroupAssignees',
                        emptyText: Uni.I18n.translate('issues.startTypingForUsers', 'ISU', 'Start typing for workgroup'),
                        msgTarget: 'under',
                        listeners: {
                            render: function () {
                                this.store.load();
                            },
                            change: function (combo, newValue) {
                                this.up('#frm-assign-issue').down('#cbo-user-issue-assignee').workgroupID = newValue;
                            },
                            select: function (combo, newValue) {
                                this.up('#frm-assign-issue').down('#cbo-user-issue-assignee').select(-1);
                            }
                        }
                    },
                    {
                        xtype: 'issues-user-assignee-combo',
                        itemId: 'cbo-user-issue-assignee',
                        fieldLabel: Uni.I18n.translate('general.user', 'ISU', 'User'),
                        required: true,
                        queryMode: 'local',
                        name: 'userId',
                        valueField: 'id',
                        displayField: 'name',
                        allowBlank: false,
                        store: 'Isu.store.UserList',
                        emptyText: Uni.I18n.translate('issues.startTypingForUsers', 'ISU', 'Start typing for user'),
                        msgTarget: 'under'
                    },
                    {
                        itemId: 'txt-comment',
                        xtype: 'textareafield',
                        name: 'comment',
                        fieldLabel: Uni.I18n.translate('general.comment', 'ISU', 'Comment'),
                        emptyText: Uni.I18n.translate('general.provideComment', 'ISU', 'Provide a comment (optionally)'),
                        height: 160
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: ' ',
                        defaultType: 'button',
                        items: [
                            {
                                itemId: 'issue-assign-action-apply',
                                ui: 'action',
                                text: Uni.I18n.translate('general.assign', 'ISU', 'Assign'),
                                action: 'applyAction'
                            },
                            {
                                itemId: 'issue-assign-action-cancel',
                                text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                                ui: 'link',
                                action: 'cancelAction',
                                href: me.cancelLink
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});