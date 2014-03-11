Ext.define('Isu.view.workspace.issues.AssignForm', {
    extend: 'Ext.form.Panel',
    defaults: {
        border: false
    },
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup'
    ],
    alias: 'widget.issues-assign-form',

    items: [
        {
            name: 'form-errors',
            layout: 'hbox',
            margin: '0 0 20 100',
            hidden: true,
            defaults: {
                xtype: 'container',
                cls: 'isu-error-panel'
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
                            html: '<b>Assign to *</b>',
                            width: 70
                        },
                        {
                            xtype: 'radiogroup',
                            formBind: false,
                            columns: 1,
                            vertical: true,
                            width: 100,
                            defaults: {
                                name: 'assignTo',
                                formBind: false,
                                submitValue: false
                            },
                            listeners: {
                                change: {
                                    fn: function (radiogroup, newValue, oldValue) {
                                        var form = radiogroup.up('issues-assign-form');
                                        form.assignToOnChange(radiogroup, newValue, oldValue);
                                    }
                                }
                            },
                            items: [
                                {
                                    boxLabel: 'User',
                                    checked: true,
                                    inputValue: 'USER'
                                },
                                {
                                    boxLabel: 'User role',
                                    margin: '5 0 0 0',
                                    inputValue: 'ROLE'
                                },
                                {
                                    boxLabel: 'User group',
                                    margin: '5 0 0 0',
                                    inputValue: 'GROUP'
                                }
                            ]
                        },
                        {
                            defaults: {
                                xtype: 'combobox',
                                queryMode: 'local',
                                valueField: 'id',
                                forceSelection: true,
                                anyMatch: true,
                                msgTarget: 'under',
                                validateOnChange: false,
                                validateOnBlur: false,
                                width: 300,
                                listeners: {
                                    errorchange: {
                                        fn: function (combo, errEl) {
                                            var form = combo.up('issues-assign-form');
                                            form.comboOnError(combo, errEl);
                                        }
                                    },
                                    focus: {
                                        fn: function (combo) {
                                            var radiobutton = combo.up().previousNode('radiogroup').down('[inputValue=' + combo.name + ']');
                                            radiobutton.setValue(true);
                                            var arrCombo = Ext.ComponentQuery.query('issues-assign-form combobox');
                                            Ext.Array.each(arrCombo, function (item) {
                                                item.allowBlank = true;
                                            });
                                            combo.allowBlank = false;
                                        }
                                    }
                                }
                            },
                            items: [
                                {
                                    name: 'USER',
                                    store: 'Isu.store.UserList',
                                    emptyText: 'Start typing for users',
                                    allowBlank: false,
                                    displayField: 'authenticationName'
                                },
                                {
                                    name: 'ROLE',
                                    store: 'Isu.store.UserRoleList',
                                    emptyText: 'Start typing for user roles',
                                    displayField: 'name'
                                },
                                {
                                    name: 'GROUP',
                                    store: 'Isu.store.UserGroupList',
                                    emptyText: 'Start typing for user groups',
                                    displayField: 'name'
                                }
                            ]
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
                            html: '<b>Comment</b>',
                            width: 74
                        },
                        {
                            xtype: 'textareafield',
                            name: 'comment',
                            emptyText: 'Provide a comment \r\n(optionally)',
                            width: 397
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {
        fielderrorchange: {
            fn: function (form, lable, error) {
                form.onFieldErrorChange(form, lable, error);
            }
        }
    },

    assignToOnChange: function (radiogroup, newValue, oldValue) {
        var activeCombobox = radiogroup.next().down('[name=' + newValue.assignTo + ']'),
            inactiveCombobox = radiogroup.next().down('[name=' + oldValue.assignTo + ']');
        inactiveCombobox.allowBlank = true;
        activeCombobox.setDisabled(false);
        activeCombobox.allowBlank = false;
        activeCombobox.focus();
    },

    comboOnError: function (combo, errEl) {
        var radiobutton = combo.up().previousNode('radiogroup').down('[inputValue=' + combo.name + ']'),
            initMargin = radiobutton.margin ? radiobutton.margin.split(' ') : [0, 0, 0, 0],
            addMargin = 24,
            finMargin;

        initMargin[2] = errEl ? parseInt(initMargin[2]) + addMargin : parseInt(initMargin[2]) - addMargin;
        finMargin = initMargin.join(' ');
        radiobutton.setMargin(finMargin);
        radiobutton.margin = finMargin;
    },

    onFieldErrorChange: function (form, lable, error) {
        var formErrorsPanel;

        if (form.xtype == 'issues-assign-form') {
            formErrorsPanel = form.down('[name=form-errors]');

            if (error) {
                formErrorsPanel.add({
                    html: 'There are errors on this page that require your attention.'
                });
                formErrorsPanel.show();
             } else {
                formErrorsPanel.hide();
                formErrorsPanel.removeAll();

            }
        }
    }
});