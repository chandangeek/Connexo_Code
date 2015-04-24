Ext.define('Est.estimationrulesets.view.RuleSetsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.rule-sets-setup',
    itemId: 'rule-sets-setup',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Est.estimationrulesets.view.RuleSetsGrid',
        'Est.estimationrules.view.Grid',
        'Est.estimationrules.view.DetailForm',
        'Est.estimationrules.store.Rules'
    ],
    router: null,
    rulesStore: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('estimationrulesets.estimationrulesets', 'EST', 'Estimation rule sets'),
                items: {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'rule-sets-grid',
                        itemId: 'rule-sets-grid',
                        router: me.router
                    },
                    previewComponent: {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'estimation-rules-grid',
                            showButtons: false,
                            store: me.rulesStore,
                            router: me.router,
                            actionMenuItemId: 'rule-set-rule-action-menu',
                            itemId: 'rule-sets-rule-grid',
                            showBottomPaging: true
                        },
                        previewComponent: {
                            xtype: 'estimation-rules-detail-form',
                            actionMenuItemId: 'rule-set-rule-action-menu',
                            itemId: 'rule-sets-rule-preview',
                            frame: true,
                            ui: 'default',
                            title: ''
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('estimationrules.empty.title', 'EST', 'No estimation rules found'),
                            reasons: [
                                Uni.I18n.translate('estimationrules.empty.list.item1', 'EST', 'No estimation rules have been defined yet.'),
                                Uni.I18n.translate('estimationrules.empty.list.item2', 'EST', 'Estimation rules exist, but you do not have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('estimationrules.addEstimationRule', 'EST', 'Add estimation rule'),
                                    itemId: 'rule-sets-add-rule-button'
                                }
                            ]
                        }
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('estimationrules.empty.list.item1', 'EST', 'No events found'),
                        reasons: [
                            Uni.I18n.translate('estimation.ruleSets.reson1', 'EST', 'No estimation rule sets have been defined yet.'),
                            Uni.I18n.translate('estimation.ruleSets.reson2', 'EST', 'Estimation rule sets exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('estimationrulesets.add.title', 'EST', 'Add estimation rule set'),
                                itemId: 'add-estimation-rule-set-button',
                                xtype: 'button',
                                action: 'addEstimationRuleSet'
                            }
                        ]
                    }
                }

            }
        ];
        this.callParent(arguments);
    }
});



