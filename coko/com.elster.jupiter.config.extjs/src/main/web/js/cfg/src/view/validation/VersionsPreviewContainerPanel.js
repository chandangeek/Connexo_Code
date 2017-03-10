/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.VersionsPreviewContainerPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.versions-preview-container-panel',
    itemId: 'versionsPreviewContainerPanel',
    ui: 'medium',
    padding: 0,
    ruleSetId: null,
	versionId: null,
    isSecondPagination: false,
    requires: [
        'Cfg.view.validation.VersionsList',
        'Cfg.view.validation.RulePreview',
        'Cfg.view.validation.VersionsActionMenu',
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
					versionId: me.versionId,
                    isSecondPagination: me.isSecondPagination
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-rule-set-version',
                    title: Uni.I18n.translate('validation.empty.versions.title', 'CFG', 'No validation rule set versions found'),
                    reasons: [
                        Uni.I18n.translate('validation.empty.versions.list.item1', 'CFG', 'No validation rule set versions have been added yet.')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('validation.addValidationRulesetVersion', 'CFG', 'Add validation rule set version'),
                            privileges : Cfg.privileges.Validation.admin,
                            href: '#/administration/validation/rulesets/' + me.ruleSetId  + '/versions/add'
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
