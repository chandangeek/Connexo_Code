Ext.define('Mdc.view.setup.commandrules.CommandRuleOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRuleOverview',
    requires: [
        'Mdc.view.setup.commandrules.CommandRuleSideMenu',
        'Mdc.view.setup.commandrules.CommandRuleActionMenu'
    ],
    router: null,
    commandRuleRecord: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'mdc-command-rule-overview-form',
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
                                    return value===0 ? Uni.I18n.translate('general.none', 'MDC', 'None') : value;
                                }
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-weekLimit-field',
                                fieldLabel: Uni.I18n.translate('general.weekLimit', 'MDC', 'Week limit'),
                                name: 'weekLimit',
                                renderer: function(value) {
                                    return value===0 ? Uni.I18n.translate('general.none', 'MDC', 'None') : value;
                                }
                            },
                            {
                                itemId: 'mdc-command-rule-preview-panel-monthLimit-field',
                                fieldLabel: Uni.I18n.translate('general.monthLimit', 'MDC', 'Month limit'),
                                name: 'monthLimit',
                                renderer: function(value) {
                                    return value===0 ? Uni.I18n.translate('general.none', 'MDC', 'None') : value;
                                }
                            },
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
                },
                {
                    xtype: 'uni-button-action',
                    //privileges: Mdc.privileges.Communication.admin,
                    menu: {
                        xtype: 'commandRuleActionMenu',
                        record: me.commandRuleRecord
                    }
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'commandRuleSideMenu',
                        commandRuleName: me.commandRuleRecord.get('name'),
                        router: me.router,
                        itemId: 'mdc-command-rule-overview-sidemenu',
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);

        me.on('afterrender', function() {
            me.down('#mdc-command-rule-overview-form').loadRecord(me.commandRuleRecord);
        }, me, {single:true});
    }
});
