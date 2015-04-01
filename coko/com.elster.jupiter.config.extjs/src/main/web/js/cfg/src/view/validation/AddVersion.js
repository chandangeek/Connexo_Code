Ext.define('Cfg.view.validation.AddVersion', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.validation-rule-set-version-add',
    itemId: 'addVersion',
    overflowY: true,

    requires: [
        'Uni.form.field.DateTime',
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
    ],

    edit: false,

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.save', 'CFG', 'Save'));
            this.down('#add-button').action = 'editVersionAction';
        } else {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.add', 'CFG', 'Add'));
            this.down('#add-button').action = 'createVersionAction';
        }
        this.down('#cancel-link').href = returnLink;
    },

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'addVersionTitle',
            items: [
                {
                    xtype: 'form',
                    itemId: 'add-validation-rule-set-form',
                    padding: '10 10 0 10',
                    layout: {
                        type: 'vbox'
                    },
                    defaults: {
                        labelWidth: 250,
                        validateOnChange: false,
                        validateOnBlur: false
                    },
                    items: [
                        {
                            itemId: 'form-errors',
                            xtype: 'uni-form-error-message',
                            name: 'form-errors',
                            width: 400,
                            margin: '0 0 10 0',
                            hidden: true
                        },
                        {
                            xtype: 'textfield',
                            fieldLabel: Uni.I18n.translate('validation.validationRuleSetVersion', 'CFG', 'Validation rule set version'),
                            itemId: 'add-version-name',
                            required: true,
                            labelAlign: 'right',
                            msgTarget: 'under',
                            maxLength: 80,
                            enforceMaxLength: true,
                            labelWidth: 260,
                            name: 'name',
                            width: 600
                        },
                        {
                            xtype: 'textarea',
                            name: 'description',
                            itemId: 'addRuleSetVersionDescription',
                            width: 600,
                            height: 150,
                            fieldLabel: Uni.I18n.translate('validation.description', 'CFG', 'Description'),
                            enforceMaxLength: true
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('validationTasks.general.recurrence', 'CFG', 'Start'),
                            itemId: 'start-period-container',
                            layout: 'hbox',
                            items: [
                                {
                                    itemId: 'start-period-trigger',
                                    xtype: 'radiogroup',
                                    name: 'startTrigger',
                                    columns: 1,
                                    vertical: true,
                                    width: 100,
                                    defaults: {
                                        name: 'start-period'
                                    },
                                    items: [
                                        {
                                            itemId: 'start-period-none',
                                            boxLabel: Uni.I18n.translate('validationTasks.general.none', 'CFG', 'None'),
                                            inputValue: false,
                                            checked: true
                                        },
                                        {
                                            itemId: 'start-period-on',
                                            boxLabel: Uni.I18n.translate('validationTasks.general.every', 'CFG', 'On'),
                                            inputValue: true
                                        }
                                    ]
                                },
                                {
                                    itemId: 'start-date-ctrl',
                                    xtype: 'fieldcontainer',
                                    fieldLabel: Uni.I18n.translate('validationTasks.general.startOn', 'CFG', 'Start on'),
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'date-time',
                                            itemId: 'start-date',
                                            layout: 'hbox',
                                            name: 'start-date',
                                            dateConfig: {
                                                allowBlank: true,
                                                value: new Date(),
                                                editable: false,
                                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                            },
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('validation-rule-set-version-add #start-period-on')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            },
                                            hoursConfig: {
                                                fieldLabel: Uni.I18n.translate('validationTasks.general.at', 'CFG', 'at'),
                                                labelWidth: 10,
                                                margin: '0 0 0 10',
                                                value: new Date().getHours()
                                            },
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('validation-rule-set-version-add #start-period-on')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            },
                                            minutesConfig: {
                                                width: 55,
                                                value: new Date().getMinutes()
                                            },
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('validation-rule-set-version-add #start-period-on')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            }
                                        }
                                    ]

                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('validationTasks.general.recurrence', 'CFG', 'End'),
                            itemId: 'end-period-container',
                            layout: 'hbox',
                            items: [
                                {
                                    itemId: 'end-period-trigger',
                                    xtype: 'radiogroup',
                                    name: 'startTrigger',
                                    columns: 1,
                                    vertical: true,
                                    width: 100,
                                    defaults: {
                                        name: 'end-period'
                                    },
                                    items: [
                                        {
                                            itemId: 'end-period-none',
                                            boxLabel: Uni.I18n.translate('validationTasks.general.none', 'CFG', 'None'),
                                            inputValue: false,
                                            checked: true
                                        },
                                        {
                                            itemId: 'end-period-on',
                                            boxLabel: Uni.I18n.translate('validationTasks.general.every', 'CFG', 'On'),
                                            inputValue: true
                                        }
                                    ]
                                },
                                {
                                    itemId: 'end-date-ctrl',
                                    xtype: 'fieldcontainer',
                                    fieldLabel: Uni.I18n.translate('validationTasks.general.startOn', 'CFG', 'End'),
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'date-time',
                                            itemId: 'end-date',
                                            layout: 'hbox',
                                            name: 'end-date',
                                            dateConfig: {
                                                allowBlank: true,
                                                value: new Date(),
                                                editable: false,
                                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                            },
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('validation-rule-set-version-add #end-period-on')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            },
                                            hoursConfig: {
                                                fieldLabel: Uni.I18n.translate('validationTasks.general.at', 'CFG', 'at'),
                                                labelWidth: 10,
                                                margin: '0 0 0 10',
                                                value: new Date().getHours()
                                            },
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('validation-rule-set-version-add #end-period-on')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            },
                                            minutesConfig: {
                                                width: 55,
                                                value: new Date().getMinutes()
                                            },
                                            listeners: {
                                                focus: {
                                                    fn: function () {
                                                        var radioButton = Ext.ComponentQuery.query('validation-rule-set-version-add #end-period-on')[0];
                                                        radioButton.setValue(true);
                                                    }
                                                }
                                            }
                                        }
                                    ]

                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            margin: '20 0 0 0',
                            fieldLabel: '&nbsp',
                            labelAlign: 'right',
                            labelWidth: 260,
                            layout: 'hbox',
                            items: [
                                {
                                    text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                    xtype: 'button',
                                    ui: 'action',
                                    action: 'createEditRuleSetVersionAction',
                                    itemId: 'add-button'
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                    href: '#/administration/validation',
                                    itemId: 'cancel-link',
                                    ui: 'link'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
        this.setEdit(this.edit, this.returnLink);
    }
});

