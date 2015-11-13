Ext.define('Bpm.view.task.ManageTaskForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.form.Panel',
        'Bpm.store.task.TasksUsers'
    ],
    /*
    stores: [
        'Bpm.store.task.TasksUsers'
    ],*/
    ui: 'medium',
    padding: 0,
    alias: 'widget.task-manage-form',
    items: [
        {
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'left'
            },
            items: {
                itemId: 'form-errors',
                xtype: 'uni-form-error-message',
                name: 'form-errors',
                hidden: true
            }
        },
        {
            defaults: {
                width: 500
            },
            items: [
                {
                    xtype: 'combobox',
                    fieldLabel: Uni.I18n.translate('general.assignee','BPM','Assignee'),
                    required: true,
                    queryMode: 'local',
                    valueField: 'id',
                    allowBlank: false,
                    validateOnChange: false,
                    name: 'assigneeCombo',
                    emptyText: Uni.I18n.translate('bpm.task.startTypingForUsers','BPM','Start typing for users'),
                    displayField: 'name'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('tasks.bulk.dueDate', 'BPM', 'Due date'),
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'date-time',
                            itemId: 'start-on',
                            layout: 'hbox',
                            name: 'start-on',
                            dateConfig: {
                                allowBlank: true,
                                value: new Date(),
                                editable: false,
                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                            },
                            hoursConfig: {
                                fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
                                labelWidth: 10,
                                margin: '0 0 0 10',
                                value: new Date().getHours()
                            },
                            minutesConfig: {
                                width: 55,
                                value: new Date().getMinutes()
                            }
                        }
                    ]
                },
            ]
        }
    ],
    initComponent: function() {
        var me = this,
            userStore = Ext.getStore('Bpm.store.task.TasksUsers'),
            step3 = Ext.ComponentQuery.query('bulk-step3')[0],
            assigneeCombo;

        me.callParent(arguments);
        assigneeCombo = me.down('combobox[name=assigneeCombo]');
        userStore.load(function (records) {
            Ext.getBody().unmask();
            if (!Ext.isEmpty(records)) {
                assigneeCombo.bindStore(userStore);
            }
        });
    }

/*
    initComponent: function(){

        var me = this,
            //userStore = Ext.getStore('Isu.store.UserList'),
            step3 = Ext.ComponentQuery.query('bulk-step3')[0],
            assigneeCombo;

        me.callParent(arguments);
        assigneeCombo = me.down('combobox[name=assigneeCombo]');
        Ext.getBody().mask( Uni.I18n.translate('general.loading', 'ISU', 'Loading...') );

        userStore.load(function (records) {
            Ext.getBody().unmask();
            if (!Ext.isEmpty(records)) {
                assigneeCombo.bindStore(userStore);
            }
        });
    },

    loadRecord: function (record) {
        var title = 'Assign issue "' + record.get('title') + '"';
        this.setTitle(title);
        this.callParent(arguments)
    }
*/
});