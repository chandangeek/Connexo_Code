Ext.define('Cfg.view.validation.VersionsPreviewContainerPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.versions-preview-container-panel',
    itemId: 'versionsPreviewContainerPanel',
    title: Uni.I18n.translate('validation.versions', 'CFG', 'Versions'),
    ui: 'medium',
    padding: 0,
    ruleSetId: null,
    isSecondPagination: false,
    requires: [
        'Cfg.view.validation.VersionsList',
        'Cfg.view.validation.RulePreview',
        'Cfg.view.validation.RuleActionMenu',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.view.validation.RuleSetPreview'
    ],
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'preview-container',
                itemId: 'previewVersionsContainer',
                grid: {
                    xtype: 'versionsList',
                    ruleSetId: me.ruleSetId,
                    isSecondPagination: me.isSecondPagination
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-validation-rule',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'CFG', 'No validation rule versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'CFG', 'No validation rule versions have been added yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addVersion', 'CFG', 'Add validation rule version'),
                            privileges: ['privilege.administrate.validationConfiguration'],
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/rules/add'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'container',
                    itemId: 'versionValidationRulesBrowsePreviewCt'
                }
            }
        ];
        me.callParent(arguments);
    }
});
