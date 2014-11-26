Ext.define('Idc.view.workspace.issues.AssignForm', {
    extend: 'Ext.form.Panel',
    defaults: {
        border: false
    },
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup'
    ],
    ui: 'medium',
    title: 'Assign issue',
    alias: 'widget.issues-assign-form',
    items: [
        {
            xtype: 'panel',
            margin: '0 0 20 0',
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
            margin: '0 0 0 100',
            defaults: {
                border: false
            },
            items: [
                {
                    layout: 'hbox',
                    defaults: {
                        border: false
                    },
                    items: [
                        {
                            width: 80,
                            items: {
                                itemId: 'AssignTo',
                                xtype: 'label',
                                text: 'Assign to *'
                            }
                        },
                        {
                            xtype: 'combobox',
                            store: 'Idc.store.AssigneeTypes',
                            queryMode: 'local',
                            valueField: 'id',
                            emptyText: 'Start typing for assignee type',
                            allowBlank: false,
                            validateOnChange: false,
                            name: 'assigneeType',
                            displayField: 'localizedValue',
                            listeners: {
                                afterrender: {
                                    fn: function (combo) {
                                        Ext.create('Ext.tip.ToolTip', {
                                            target:  combo.getEl(),
                                            html: 'Start typing for assignee type',
                                            anchor: 'top'
                                        });
                                    }
                                },
                                select: {
                                    fn: function (combo, records) {
                                        var form = combo.up('issues-assign-form');
                                        form.assigneeTypeChange(combo, records[0]);
                                    }
                                }
                            }
                        },
                        {
                            xtype: 'combobox',
                            margin: '0 0 0 37',
                            queryMode: 'local',
                            valueField: 'id',
                            allowBlank: false,
                            validateOnChange: false,
                            name: 'assigneeCombo',
                            displayField: 'name'
                        }
                    ]
                },
                {
                    layout: 'hbox',
                    margin: '20 0 0 0',
                    defaults: {
                        border: false
                    },
                    items: [
                        {
                            width: 80,
                            items: {
                                itemId: 'Comment',
                                xtype: 'label',
                                text: 'Comment'
                            }
                        },
                        {
                            itemId: 'commentarea',
                            xtype: 'textareafield',
                            name: 'comment',
                            emptyText: 'Provide a comment \r\n(optionally)',
                            width: 390,
                            height: 150
                        }
                    ]
                }
            ]
        }
    ],
    assigneeTypeChange: function (combo, record) {
        var value = record.get(combo.valueField);
            assigneeCombo = combo.nextNode('combobox[name=assigneeCombo]'),
            tooltips = Ext.ComponentQuery.query('tooltip[name=assigneeTooltip]'),
            hint = 'Start typing for ' + combo.getRawValue().toLowerCase() + 's';
        Ext.each(tooltips, function (tooltip) {
           tooltip.destroy();
        });
        Ext.create('Ext.tip.ToolTip', {
            target:  assigneeCombo.getEl(),
            html: hint,
            name: 'assigneeTooltip',
            anchor: 'top'
        });
        assigneeCombo.emptyText = hint;
        switch (value) {
            case 'USER' :
                var userStore = Ext.getStore('Idc.store.UserList');
                assigneeCombo.bindStore(userStore);
                assigneeCombo.store.load();
                break;
            case 'GROUP' :
                var groupStore = Ext.getStore('Idc.store.UserGroupList');
                assigneeCombo.bindStore(groupStore);
                assigneeCombo.store.load();
                break;
            case 'ROLE' :
                var roleStore = Ext.getStore('Idc.store.UserRoleList');
                assigneeCombo.bindStore(roleStore);
                assigneeCombo.store.load();
                break;
        }
        assigneeCombo.clearValue();
    },

    loadRecord: function (record) {
        var title = 'Assign issue "' + record.get('title') + '"';
        this.setTitle(title);
        this.callParent(arguments)
    }
});