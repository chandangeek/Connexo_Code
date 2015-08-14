Ext.define('Cfg.view.validation.AddVersion', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.addVersion',
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
                    itemId: 'addVersionForm',
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
                            xtype: 'textarea',
                            name: 'description',
                            itemId: 'addVersionDescription',
                            width: 600,
                            height: 150,
							labelWidth: 260,
                            fieldLabel: Uni.I18n.translate('general.description', 'CFG', 'Description'),
                            enforceMaxLength: true
                        },						
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('validation.versionStart', 'CFG', 'Start'),
                            itemId: 'startPeriodContainer',
                            layout: 'hbox',
                            items: [
                                {
									itemId: 'startPeriodTrigger',
                                    xtype: 'radiogroup',
									name: 'startTrigger',
                                    columns: 1,
                                    vertical: true,
                                    width: 100,
                                    defaults: {
                                        name: 'startPeriod'
                                    },
									items: [
                                        {
                                            itemId: 'startPeriodNone',
                                            boxLabel: Uni.I18n.translate('general.none', 'CFG', 'None'),
                                            inputValue: false,
                                            checked: true
                                        },
                                        {
                                            itemId: 'startPeriodOn',
                                            boxLabel: Uni.I18n.translate('validation.version.on', 'CFG', 'On'),
                                            inputValue: true
                                        }
                                    ]
                                },
                                {
                                    itemId: 'startDateCtrl',
                                    xtype: 'fieldcontainer',
									margin: '30 0 10 0',                                
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'date-time',
                                            itemId: 'startDate',
                                            layout: 'hbox',
                                            name: 'startDate',
                                            dateConfig: {
                                                allowBlank: true,
                                                value: new Date(),
                                                editable: false,
                                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault),
                                                listeners: {
                                                    focus: {
                                                        fn: function () {
                                                            var radioButton = Ext.ComponentQuery.query('addVersion #startPeriodOn')[0];
                                                            radioButton.setValue(true);
                                                        }
                                                    }
                                                }
                                            },
                                            hoursConfig: {
                                                fieldLabel: Uni.I18n.translate('validationTasks.general.at', 'CFG', 'at'),
                                                labelWidth: 10,
                                                margin: '0 0 0 10',
                                                value: new Date().getHours(),
                                                listeners: {
                                                    focus: {
                                                        fn: function () {
                                                            var radioButton = Ext.ComponentQuery.query('addVersion #startPeriodOn')[0];
                                                            radioButton.setValue(true);
                                                        }
                                                    }
                                                }
                                            },
                                            minutesConfig: {
                                                width: 55,
                                                value: new Date().getMinutes(),
                                                listeners: {
                                                    focus: {
                                                        fn: function () {
                                                            var radioButton = Ext.ComponentQuery.query('addVersion #startPeriodOn')[0];
                                                            radioButton.setValue(true);
                                                        }
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

