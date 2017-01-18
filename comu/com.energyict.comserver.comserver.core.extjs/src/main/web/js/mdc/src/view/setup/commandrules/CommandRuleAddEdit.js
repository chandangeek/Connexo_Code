Ext.define('Mdc.view.setup.commandrules.CommandRuleAddEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRuleAddEdit',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage',
        'Uni.form.field.DisplayFieldWithInfoIcon'
    ],
    stores: [
        'Mdc.store.CommandsForRule'
    ],
    LABEL_WIDTH: 200,
    MAX_LIMIT: 999999999,
    LIMIT_SPINNER_WIDTH: 110,

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
                        width: 500
                    },
                    {
                        xtype: 'panel',
                        itemId: 'mdc-command-rule-addEdit-infoPanel',
                        hidden: true,
                        items: [
                            {
                                xtype: 'uni-form-info-message',
                                itemId: 'mdc-command-rule-addEdit-infoMsg'
                            }
                        ]
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
                        itemId: 'mdc-command-rule-addEdit-dayLimit-radioGroupContainer',
                        required: true,
                        msgTarget: 'under',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'radiogroup',
                                itemId: 'mdc-command-rule-addEdit-dayLimit-radioGroup',
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
                                        maxValue: me.MAX_LIMIT,
                                        value: 1,
                                        width: me.LIMIT_SPINNER_WIDTH,
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
                        itemId: 'mdc-command-rule-addEdit-weekLimit-radioGroupContainer',
                        required: true,
                        msgTarget: 'under',
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
                                        maxValue: me.MAX_LIMIT,
                                        value: 1,
                                        width: me.LIMIT_SPINNER_WIDTH,
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
                        itemId: 'mdc-command-rule-addEdit-monthLimit-radioGroupContainer',
                        required: true,
                        msgTarget: 'under',
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
                                        maxValue: me.MAX_LIMIT,
                                        value: 1,
                                        width: me.LIMIT_SPINNER_WIDTH,
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
                                width: 500,
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
        if (value > field.maxValue) {
            field.setValue(field.maxValue);
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
    },

    loadCommandRule: function(commandRule) {
        var me = this,
            form = me.down('form').getForm(),
            dayLimitGroup = me.down('#mdc-command-rule-addEdit-dayLimit-radioGroup'),
            weekLimitGroup = me.down('#mdc-command-rule-addEdit-weekLimit-radioGroup'),
            monthLimitGroup = me.down('#mdc-command-rule-addEdit-monthLimit-radioGroup'),
            dayLimitNumberField = me.down('#mdc-command-rule-addEdit-dayLimit-number'),
            weekLimitNumberField = me.down('#mdc-command-rule-addEdit-weekLimit-number'),
            monthLimitNumberField = me.down('#mdc-command-rule-addEdit-monthLimit-number'),
            noDayLimit = commandRule.get('dayLimit')=== 0,
            noWeekLimit = commandRule.get('weekLimit')=== 0,
            noMonthLimit = commandRule.get('monthLimit')=== 0;

        form.loadRecord(commandRule);
        dayLimitGroup.setValue({ noDayLimit: noDayLimit });
        if (noDayLimit) {
            dayLimitNumberField.setValue(1);
        }
        weekLimitGroup.setValue({ noWeekLimit: noWeekLimit });
        if (noWeekLimit) {
            weekLimitNumberField.setValue(1);
        }
        monthLimitGroup.setValue({ noMonthLimit: noMonthLimit });
        if (noMonthLimit) {
            monthLimitNumberField.setValue(1);
        }
        me.updateCommandsGrid();
    }

});