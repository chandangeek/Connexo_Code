Ext.define('Isu.view.workspace.issues.AssignForm', {
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
            layout : {
              type: 'vbox',
              align: 'left'
            },
            items: {
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
                                xtype: 'label',
                                text: 'Assign to *'
                            }
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
                                },
                                afterrender: {
                                    fn: function(rgroup){
                                        rgroup.reset();
                                    }
                                }
                            },
                            items: [
                                {
                                    boxLabel: 'User',
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
                                    allowBlank: false,
                                    displayField: 'authenticationName'
                                },
                                {
                                    name: 'ROLE',
                                    store: 'Isu.store.UserRoleList',
                                    displayField: 'name'
                                },
                                {
                                    name: 'GROUP',
                                    store: 'Isu.store.UserGroupList',
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
                            width: 80,
                            items: {
                                xtype: 'label',
                                text: 'Assign to *'
                            }
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
        },
        afterrender: function (form) {
            var values = Ext.state.Manager.get('formAssignValues');
            Ext.ComponentQuery.query('issues-assign-form radiogroup')[0].down('[inputValue=USER]').setValue(true);

            if (values) {
                Ext.Object.each(values, function (key, value) {
                    if (key == 'comment') {
                        form.down('textareafield').setValue(value);
                    }
                    if (key == 'GROUP') {
                        form.down('combobox[name=GROUP]').setValue(value);
                    }
                    if (key == 'ROLE') {
                        form.down('combobox[name=ROLE]').setValue(value);
                    }
                    if (key == 'USER') {
                        form.down('combobox[name=USER]').setValue(value);
                    }
                });
            }
            var selRadio = Ext.state.Manager.get('formAssignRadio');
            if (selRadio) {
                var radio = form.down('radiogroup').down('[inputValue=' + selRadio + ']');
                radio.setValue(true);
            }
        }
    },

    assignToOnChange: function (radiogroup, newValue, oldValue) {
        var activeCombobox = radiogroup.next().down('[name=' + newValue.assignTo + ']'),
            inactiveCombobox = radiogroup.next().down('[name=' + oldValue.assignTo + ']'),
            currentBoxLabel = radiogroup.down('[checked=true]').boxLabel,
            tooltips = Ext.ComponentQuery.query('tooltip[anchor=top]');

        Ext.each(tooltips, function (tooltip) {
           tooltip.destroy();
        });

        Ext.create('Ext.tip.ToolTip', { target:  activeCombobox.getEl(), html: 'Start typing for ' + currentBoxLabel.toLowerCase() + 's', anchor: 'top' });
        if (!Ext.isEmpty(inactiveCombobox)) {
            inactiveCombobox.allowBlank = true;
        }
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
                formErrorsPanel.hide();
                formErrorsPanel.removeAll();
                formErrorsPanel.add({
                    html: 'There are errors on this page that require your attention.'
                });
                formErrorsPanel.show();
            }
        }
    },

    loadRecord: function(record) {
        var title = 'Assign issue "' + record.get('title') + '"'

        this.setTitle(title);
        this.callParent(arguments)
    }
});