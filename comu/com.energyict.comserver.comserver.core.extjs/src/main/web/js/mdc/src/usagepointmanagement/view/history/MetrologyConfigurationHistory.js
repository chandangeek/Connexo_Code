/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistory', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metrology-configuration-history-tab',
    itemId: 'metrology-configuration-history',
    router: null,
    usagePoint: null,

    requires: [
        'Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistoryGrid',
        'Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistoryPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'metrology-configuration-history-grid',
                    itemId: 'metrology-configuration-history-grid-id',
                    router: me.router,
                    usagePoint: me.usagePoint
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'metrology-configuration-history-empty-id',
                    title: Uni.I18n.translate('usagePoint.noMetrologyConfiguration.empty', 'MDC', 'No metrology configuration versions found.'),
                    reasons: [
                        Uni.I18n.translate('usagePoint.noMetrologyConfiguration.reason', 'MDC', 'No metrology configuration versions have been defined yet.'),
                    ],
                    stepsText: Uni.I18n.translate('usagePoint.noMetrologyConfiguration.actions', 'MDC', 'Actions:'),
                    stepItems: [
                        {
                            text: Uni.I18n.translate('usagePoint.noMetrologyConfiguration.addVersions', 'MDC', 'Add version'),
                            action: 'addVersions',
                            itemId: 'add-version-btn',
                            href: me.router.getRoute('usagepoints/usagepoint/history/addmetrologyconfigurationversion').buildUrl(),
                            privileges: Mdc.privileges.UsagePoint.canAdmin()
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'metrology-configuration-history-preview',
                    itemId: 'metrology-configuration-history-preview-id',
                    usagePoint: me.usagePoint
                }
            }
        ];
        me.callParent(arguments);
    }
});