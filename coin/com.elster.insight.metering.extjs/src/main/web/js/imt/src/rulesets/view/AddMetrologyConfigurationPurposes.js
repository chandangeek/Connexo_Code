Ext.define('Imt.rulesets.view.AddMetrologyConfigurationPurposes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-metrology-configuration-purposes',
    requires: [
        'Cfg.view.validation.RuleSetSubMenu',
        'Imt.rulesets.view.MetrologyConfigurationPurposeDetails',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.util.FormInfoMessage',
        'Imt.rulesets.view.AddMetrologyConfigurationPurposesGrid'
    ],
    purposesStore: null,
    router: null,
    ruleSetId: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'add-metrology-configuration-purposes-panel',
                title: Uni.I18n.translate('general.addMetrologyConfigurationPurposes', 'IMT', 'Add metrology configuration purposes'),
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        itemId: 'add-metrology-configuration-purposes-info-message',
                        text: Uni.I18n.translate('ruleSet.addMetrologyConfigurationPurposes.infoText', 'IMT', 'Only metrology configuration purposes with at least 1 matching reading type are displayed')
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'metrology-configuration-purposes-preview-container',
                        grid: {
                            xtype: 'add-metrology-configuration-purposes-grid',
                            itemId: 'add-metrology-configuration-purposes-grid',
                            store: me.purposesStore,
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-purposes-to-add-found-panel',
                            title: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item', 'IMT', 'No metrology configuration purposes found'),
                            reasons: [
                                Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item1', 'IMT', 'No metrology configurations have been added yet.'),
                                Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item2', 'IMT', 'There are no metrology configuration purposes that have reading types that match the rules in the validation rule set.'),
                                Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item3', 'IMT', 'Matching metrology configuration purposes exist but you do not have permission to view them.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'metrology-configuration-purpose-details',
                            itemId: 'metrology-configuration-purpose-preview',
                            frame: true
                        }
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: me.ruleSetId
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});