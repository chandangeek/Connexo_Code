Ext.define('Mdc.view.setup.commandrules.CommandRuleAddEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRuleAddEdit',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.form.field.DisplayFieldWithInfoIcon'
    ],
    stores: [
        'Mdc.store.CommandsForRule'
    ],
    LABEL_WIDTH: 200,

    initComponent: function() {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'mdc-command-rule-addEdit-rule-form',
                ui: 'large',
                defaults: {
                    labelWidth: me.LABEL_WIDTH
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'mdc-command-rule-addEdit-error',
                        hidden: true,
                        width: 380
                    },
                    {
                        xtype: 'textfield',
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        itemId: 'mdc-command-rule-addEdit-name-field',
                        name: 'name',
                        required: true,
                        width: 500,
                        listeners: {
                            afterrender: function (field) {
                                field.focus(false, 200);
                            }
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.dayLimit', 'MDC', 'Day limit'),
                        itemId: 'mdc-command-rule-addEdit-dayLimit-radioGroup',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'radiogroup',
                                itemId: 'mdc-command-rule-addEdit-dayLimit-radioGroup',
                                name: 'dayLimitGroup',
                                columns: 1,
                                vertical: true,
                                width: 100,
                                defaults: {
                                    name: 'noDayLimit'
                                },
                                items: [
                                    {
                                        boxLabel: Uni.I18n.translate('general.none', 'MDC', 'None'),
                                        itemId: 'mdc-command-rule-addEdit-dayLimit-radioBtn-none',
                                        inputValue: true,
                                        checked: true
                                    },
                                    {
                                        boxLabel: Uni.I18n.translate('general.limitTo', 'MDC', 'Limit to'),
                                        itemId: 'mdc-command-rule-addEdit-dayLimit-radioBtn-value',
                                        margin: '10 0 0 0',
                                        inputValue: false
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'mdc-command-rule-addEdit-dayLimit-values',
                                name: 'dayLimitValues',
                                layout: 'vbox',
                                items: [
                                    {
                                        xtype: 'displayfield-with-info-icon',
                                        emptyText: ''
                                    },
                                    {
                                        xtype: 'numberfield',
                                        itemId: 'mdc-command-rule-addEdit-dayLimit-number',
                                        disabled: true,
                                        name: 'dayLimit',
                                        allowDecimals: false,
                                        minValue: 1,
                                        value: 1,
                                        width: 65,
                                        margin: '-10 0 0 0',
                                        listeners: {
                                            blur: me.numberFieldValidation
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.weekLimit', 'MDC', 'Week limit'),
                        itemId: 'mdc-command-rule-addEdit-weekLimit-radioGroup',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'radiogroup',
                                itemId: 'mdc-command-rule-addEdit-weekLimit-radioGroup',
                                columns: 1,
                                vertical: true,
                                width: 100,
                                defaults: {
                                    name: 'noWeekLimit'
                                },
                                items: [
                                    {
                                        boxLabel: Uni.I18n.translate('general.none', 'MDC', 'None'),
                                        itemId: 'mdc-command-rule-addEdit-weekLimit-radioBtn-none',
                                        inputValue: true,
                                        checked: true
                                    },
                                    {
                                        boxLabel: Uni.I18n.translate('general.limitTo', 'MDC', 'Limit to'),
                                        itemId: 'mdc-command-rule-addEdit-weekLimit-radioBtn-value',
                                        margin: '10 0 0 0',
                                        inputValue: false
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'mdc-command-rule-addEdit-weekLimit-values',
                                name: 'weekLimitValues',
                                layout: 'vbox',
                                items: [
                                    {
                                        xtype: 'displayfield-with-info-icon',
                                        emptyText: ''
                                    },
                                    {
                                        xtype: 'numberfield',
                                        itemId: 'mdc-command-rule-addEdit-weekLimit-number',
                                        disabled: true,
                                        name: 'weekLimit',
                                        allowDecimals: false,
                                        minValue: 1,
                                        value: 1,
                                        width: 65,
                                        margin: '-10 0 0 0',
                                        listeners: {
                                            blur: me.numberFieldValidation
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.monthLimit', 'MDC', 'Month limit'),
                        itemId: 'mdc-command-rule-addEdit-monthLimit-radioGroup',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'radiogroup',
                                itemId: 'mdc-command-rule-addEdit-monthLimit-radioGroup',
                                columns: 1,
                                vertical: true,
                                width: 100,
                                defaults: {
                                    name: 'noMonthLimit'
                                },
                                items: [
                                    {
                                        boxLabel: Uni.I18n.translate('general.none', 'MDC', 'None'),
                                        itemId: 'mdc-command-rule-addEdit-monthLimit-radioBtn-none',
                                        inputValue: true,
                                        checked: true
                                    },
                                    {
                                        boxLabel: Uni.I18n.translate('general.limitTo', 'MDC', 'Limit to'),
                                        itemId: 'mdc-command-rule-addEdit-monthLimit-radioBtn-value',
                                        margin: '10 0 0 0',
                                        inputValue: false
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'mdc-command-rule-addEdit-monthLimit-values',
                                name: 'recurrenceValues',
                                layout: 'vbox',
                                items: [
                                    {
                                        xtype: 'displayfield-with-info-icon',
                                        emptyText: ''
                                    },
                                    {
                                        xtype: 'numberfield',
                                        itemId: 'mdc-command-rule-addEdit-monthLimit-number',
                                        disabled: true,
                                        name: 'monthLimit',
                                        allowDecimals: false,
                                        minValue: 1,
                                        value: 1,
                                        width: 65,
                                        margin: '-10 0 0 0',
                                        listeners: {
                                            blur: me.numberFieldValidation
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'mdc-command-rule-addEdit-commands-fieldContainer',
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.commands', 'MDC', 'Commands'),
                        layout: 'hbox',
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('general.noCommands','MDC','No commands have been added'),
                                itemId: 'mdc-command-rule-addEdit-noCommands-label',
                                style: {
                                    'font': 'italic 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-top': '6px',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'gridpanel',
                                itemId: 'mdc-command-rule-addEdit-commands-grid',
                                store: 'Mdc.store.CommandsForRule',
                                hideHeaders: true,
                                padding: 0,
                                hidden: true,
                                scroll: 'vertical',
                                viewConfig: {
                                    disableSelection: true,
                                    enableTextSelection: true
                                },
                                columns: [
                                    {
                                        dataIndex: 'displayName',
                                        flex: 1
                                    },
                                    {
                                        xtype: 'uni-actioncolumn-remove',
                                        align: 'right',
                                        handler: function (grid, rowIndex) {
                                            grid.getStore().removeAt(rowIndex);
                                            me.updateCommandsGrid();
                                        }
                                    }
                                ],
                                width: 800,
                                height: 220
                            },
                            {
                                xtype: 'button',
                                itemId: 'mdc-command-rule-addEdit-addCommands-button',
                                text: Uni.I18n.translate('general.addCommands', 'MDC', 'Add commands'),
                                margin: '0 0 0 10'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                itemId: 'mdc-command-rule-addEdit-add-button',
                                action: 'addRule',
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'mdc-command-rule-addEdit-cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    },

    updateCommandsGrid: function() {
        var me = this,
            commandsGrid = me.down('#mdc-command-rule-addEdit-commands-grid'),
            noCommandsLabel = me.down('#mdc-command-rule-addEdit-noCommands-label');
        if (commandsGrid.getStore().count() === 0) {
            noCommandsLabel.show();
            commandsGrid.hide();
        } else {
            noCommandsLabel.hide();
            commandsGrid.show();
        }
    }


});