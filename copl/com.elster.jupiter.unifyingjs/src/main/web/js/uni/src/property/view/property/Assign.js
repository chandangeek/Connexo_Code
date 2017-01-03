Ext.define('Uni.property.view.property.Assign', {
    extend: 'Uni.property.view.property.Base',

    requires: [
        //'Uni.util.FormErrorMessage',
        'Uni.property.view.property.UserAssigneeCombo'
        //'Isu.store.UserList'
        //'Isu.store.IssueWorkgroupAssignees'
    ],
    //alias: 'widget.assign-item',
    //router: null,

    configUrl: [
        {
            id: 'AssignIssueAction.assignee',
            workgroupUrl: '/api/isu/workgroups',
            userUrl: '/api/isu/assignees/users',
            workroupUsersUrl: '/api/isu/workgroups/{0}/users'
        }
    ],

    labelWidth: 250,
    controlsWidth: 600,

    getEditCmp: function () {
        var me = this;

        me.layout = 'vbox';
        return [
            {
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-workgroup-assignee',
                        fieldLabel: Uni.I18n.translate('assign.workgroup', 'UNI', 'Workgroup'),
                        queryMode: 'local',
                        name: 'workgroupId',
                        labelWidth: 260,
                        width: 595,
                        valueField: 'id',
                        displayField: 'name',
                        allowBlank: false,
                        store: me.getWorkgroupStore(), //'Isu.store.IssueWorkgroupAssignees',
                        emptyText: Uni.I18n.translate('assign.startTypingForWorkgroup', 'UNI', 'Start typing for workgroup'),
                        msgTarget: 'under',
                        editable: false,
                        listeners: {
                            render: function () {
                                this.store.load();
                            },
                            change: function (combo, newValue) {
                                this.ownerCt.down('#cbo-user-assignee').fireEvent('workgroupChanged', newValue);
                            },
                            select: function (combo, newValue) {
                                this.ownerCt.down('#cbo-user-assignee').fireEvent('workgroupChanged', newValue[0].get('id'));
                            }
                        }
                    },
                    {
                        xtype: 'usr-assignee-combo',
                        itemId: 'cbo-user-assignee',
                        fieldLabel: Uni.I18n.translate('assign.user', 'UNI', 'User'),
                        queryMode: 'local',
                        name: 'userId',
                        labelWidth: 260,
                        width: 595,
                        valueField: 'id',
                        displayField: 'name',
                        allowBlank: false,
                        editable: false,
                        configUrl: me.getConfig(),
                        store: me.getUserStore(), //'Isu.store.UserList',
                        emptyText: Uni.I18n.translate('assign.startTypingForUsers', 'UNI', 'Start typing for users'),
                        msgTarget: 'under'
                    },
                    {
                        itemId: 'txt-comment',
                        xtype: 'textareafield',
                        name: 'comment',
                        labelWidth: 260,
                        width: 595,
                        fieldLabel: Uni.I18n.translate('assign.comment', 'UNI', 'Comment'),
                        emptyText: Uni.I18n.translate('assign.provideComment', 'UNI', 'Provide a comment (optionally)'),
                        height: 160
                    }
                ]
            }
        ];
    },

    setLocalizedName: function (name) {
    },

    getField: function () {
        return this;
    },

    getValue: function () {
        var me = this,
            workgroupCombo = me.down('#cbo-workgroup-assignee'),
            userCombo = me.down('#cbo-user-assignee'),
            comment = me.down('#txt-comment');

        return JSON.stringify({
            workgroupId: workgroupCombo.getValue(),
            userId: userCombo.getValue(),
            comment: comment.getValue()
        });
    },

    setValue: function (value) {
        var me = this,
            jsonValue = JSON.parse(value),
            workgroupCombo = me.down('#cbo-workgroup-assignee'),
            userCombo = me.down('#cbo-user-assignee');

        workgroupCombo.suspendEvents();
        workgroupCombo.setValue(jsonValue.workgroupId);
        workgroupCombo.resumeEvents();
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
    }
});