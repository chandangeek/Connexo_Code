Ext.define('Mdc.view.setup.commandrules.CommandRulePreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.commandRulePreviewForm',
    border: false,
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    commandRuleRecord: undefined,

    initComponent: function() {
        var me = this;
        me.items = [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'mdc-command-rule-preview-panel-name-field',
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        itemId: 'mdc-command-rule-preview-panel-status-field',
                        fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                        name: 'statusWithMessage',
                        htmlEncode: false
                    },
                    {
                        itemId: 'mdc-command-rule-preview-panel-dayLimit-field',
                        fieldLabel: Uni.I18n.translate('general.dayLimit', 'MDC', 'Day limit'),
                        name: 'dayLimit',
                        renderer: function(value) {
                            return value===0 ? '-' : value;
                        }
                    },
                    {
                        itemId: 'mdc-command-rule-preview-panel-weekLimit-field',
                        fieldLabel: Uni.I18n.translate('general.weekLimit', 'MDC', 'Week limit'),
                        name: 'weekLimit',
                        renderer: function(value) {
                            return value===0 ? '-' : value;
                        }
                    },
                    {
                        itemId: 'mdc-command-rule-preview-panel-monthLimit-field',
                        fieldLabel: Uni.I18n.translate('general.monthLimit', 'MDC', 'Month limit'),
                        name: 'monthLimit',
                        renderer: function(value) {
                            return value===0 ? '-' : value;
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        itemId: 'mdc-command-rule-preview-panel-commands-field',
                        fieldLabel: Uni.I18n.translate('general.commands', 'MDC', 'Commands'),
                        name: 'commands',
                        renderer: function (value) {
                            var str = value ? '' : '-';
                            if (value) {
                                Ext.Array.sort(value, function(cmd1, cmd2) {
                                    return cmd1.category.localeCompare(cmd2.category);
                                }); // Sort
                                Ext.Array.each(value, function (cmd) {
                                    str += cmd.category + ' - ' + cmd.command + '<br />';
                                });
                            }
                            return str;
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }

});