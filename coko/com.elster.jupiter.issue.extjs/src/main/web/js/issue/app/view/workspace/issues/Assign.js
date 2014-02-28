Ext.define('Mtr.view.workspace.issues.Assign', {
    extend: 'Ext.container.Container',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.Hidden',
        'Uni.view.breadcrumb.Trail'
    ],
    alias: 'widget.issues-assign',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    overflowY: 'auto',
    items: [
        {
            xtype: 'breadcrumbTrail',
            padding: 6
        }
    ],
    listeners: {
        render: {
            fn: function (self) {
                self.addForm();
            }
        }
    },

    addForm: function () {
        var self = this;

        self.add({
            xtype: 'form',
            flex: 1,
            minHeight: 305,
            sendingData: {
                issues: [
                    {
                        id: self.record.data.id,
                        version: self.record.data.version
                    }
                ],
                force: true
            },
            border: false,
            header: false,
            bodyPadding: 10,
            defaults: {
                border: false
            },
            listeners: {
                fielderrorchange: self.onFieldErrorChange
            },
            items: [
                {
                    xtype: 'hiddenfield',
                    name: 'assignee'
                },
                {
                    html: '<h3>Assign issue ' + self.record.data.reason + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.sNumber : '') + '</h3>'
                },
                {
                    name: 'form-errors',
                    layout: 'hbox',
                    margin: '20 0 20 100',
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
                                        change: self.assignToOnChange
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
                                        disabled: true,
                                        msgTarget: 'under',
                                        validateOnChange: false,
                                        validateOnBlur: false,
                                        width: 300
                                    },
                                    items: [
                                        {
                                            name: 'USER',
                                            store: 'Mtr.store.UserList',
                                            emptyText: 'Start typing for users',
                                            allowBlank: false,
                                            disabled: false,
                                            displayField: 'authenticationName',
                                            listeners: {
                                                errorchange: self.comboOnError,
                                                change: self.comboOnChange
                                            }
                                        },
                                        {
                                            name: 'ROLE',
                                            store: 'Mtr.store.UserRoleList',
                                            emptyText: 'Start typing for user roles',
                                            displayField: 'name',
                                            listeners: {
                                                errorchange: self.comboOnError,
                                                change: self.comboOnChange
                                            }
                                        },
                                        {
                                            name: 'GROUP',
                                            store: 'Mtr.store.UserGroupList',
                                            emptyText: 'Start typing for user groups',
                                            displayField: 'name',
                                            listeners: {
                                                change: self.comboOnChange
                                            }
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
                        },
                        {
                            layout: 'hbox',
                            margin: '20 0 0 74',
                            defaults: {
                                xtype: 'button',
                                margin: '0 10 0 0'
                            },
                            items: [
                                {
                                    text: 'Assign',
                                    name: 'assign',
                                    formBind: false
                                },
                                {
                                    text: 'Cancel',
                                    name: 'cancel',
                                    cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium'
                                }
                            ]
                        }
                    ]
                }
            ]
        });
    },

    assignToOnChange: function (radiogroup, newValue, oldValue) {
        var activeCombobox = radiogroup.next().down('[name=' + newValue.assignTo + ']'),
            inactiveCombobox = radiogroup.next().down('[name=' + oldValue.assignTo + ']');

        inactiveCombobox.setDisabled(true);
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

    comboOnChange: function (combo, newValue) {
        var form = combo.up('form'),
            assignee = combo.findRecordByValue(newValue).data;
        form.sendingData.assignee = {
            id: assignee.id,
            type: combo.name,
            title: combo.rawValue
        };
    },

    onFieldErrorChange: function (form, lable, error) {
        var formErrorsPanel = form.down('[name=form-errors]');

        if (error) {
            formErrorsPanel.add({
                html: 'There are errors on this page that require your attention.'
            });
        } else {
            formErrorsPanel.removeAll();
        }
    }
});