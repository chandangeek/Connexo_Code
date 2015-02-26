Ext.define('Idc.view.workspace.issues.AssignForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.form.Panel'
    ],
    ui: 'medium',
    padding: 0,
    title: 'Assign issue',
    alias: 'widget.issues-assign-form',
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
                    fieldLabel: 'Assignee',
                    required: true,
                    queryMode: 'local',
                    valueField: 'id',
                    allowBlank: false,
                    validateOnChange: false,
                    name: 'assigneeCombo',
                    emptyText: 'Start typing for users',
                    displayField: 'name'
                },
                {
                    itemId: 'commentarea',
                    xtype: 'textareafield',
                    fieldLabel: 'Comment',
                    name: 'comment',
                    emptyText: 'Provide a comment \r\n(optionally)',
                    height: 150
                }
            ]
        }
    ],

    initComponent: function(){
        var me = this,
            userStore = Ext.getStore('Idc.store.UserList'),
            step3 = Ext.ComponentQuery.query('bulk-step3')[0],
            assigneeCombo;
        me.callParent(arguments);
        assigneeCombo = me.down('combobox[name=assigneeCombo]');
        Ext.getBody().mask( 'Loading...' );
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
});