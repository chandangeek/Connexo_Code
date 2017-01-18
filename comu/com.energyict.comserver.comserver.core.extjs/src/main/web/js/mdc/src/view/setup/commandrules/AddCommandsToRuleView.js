Ext.define('Mdc.view.setup.commandrules.AddCommandsToRuleView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddCommandsToRuleView',
    itemId: 'mdc-command-rule-add-commands',
    overflowY: true,

    requires: [
        'Mdc.view.setup.commandrules.AddCommandsToRuleGrid',
        'Mdc.view.setup.commandrules.AddCommandsToRuleFilter',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.addCommands', 'MDC', 'Add commands'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'preview-container',
                        selectByDefault: false,
                        grid: {
                            itemId: 'mdc-command-rule-add-commands-grid',
                            xtype: 'AddCommandsToRuleGrid',
                            store: 'Mdc.store.Commands'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            margin: '0 0 20 0',
                            title: Uni.I18n.translate('commandRules.add.empty.title', 'MDC', 'No commands found'),
                            reasons: [
                                Uni.I18n.translate('commandRules.add.empty.list.item1', 'MDC', 'All commands compliant with the filter have already been added to the rule.')
                            ]
                        }
                    },
                    {
                        xtype: 'container',
                        itemId: 'mdc-command-rule-add-commands-button-container',
                        defaults: {
                            xtype: 'button'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                name: 'add',
                                itemId: 'mdc-command-rule-add-commands-addButton',
                                action: 'addCommands',
                                ui: 'action'
                            },
                            {
                                name: 'cancel',
                                itemId: 'mdc-command-rule-add-commands-cancelLink',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link'
                            }
                        ]
                    }
                ],
                dockedItems: [
                    {
                        xtype: 'AddCommandsToRuleFilter',
                        itemId: 'mdc-command-rule-add-commands-filter-panel-top'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
