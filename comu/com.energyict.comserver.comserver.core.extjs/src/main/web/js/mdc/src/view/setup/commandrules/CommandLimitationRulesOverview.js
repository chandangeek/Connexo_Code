Ext.define('Mdc.view.setup.commandrules.CommandLimitationRulesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.commandRulesOverview',
    requires: [
        'Mdc.view.setup.commandrules.CommandRulesGrid',
        'Mdc.view.setup.commandrules.CommandRulePreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    router: undefined,

    initComponent: function () {
        var me = this,
            rulesStore = Ext.getStore('Mdc.store.CommandLimitationRules') || Ext.create('Mdc.store.CommandLimitationRules');

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.commandLimitationRules', 'MDC', 'Command limitation rules'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'commandRulesGrid',
                        itemId: 'mdc-command-rules-grid',
                        router: me.router,
                        store: rulesStore
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'mdc-empty-command-rules-grid',
                        title: Uni.I18n.translate('commandRules.empty.title', 'MDC', 'No command limitation rules found'),
                        reasons: [
                            Uni.I18n.translate('commandRules.empty.list.item1', 'MDC', 'No command limitation rules have been defined yet.')
                        ],
                        stepItems: [
                            {
                                itemId: 'mdc-command-rules-add-button',
                                text: Uni.I18n.translate('commandRules.create', 'MDC', 'Add command limitation rule'),
                                privileges: Mdc.privileges.CommandLimitationRules.admin,
                                href: '#/administration/commandrules/add'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'commandRulePreview',
                        itemId: 'mdc-command-rule-preview'
                    }
                }
            ]
        };

        this.callParent(arguments);
    }

});
