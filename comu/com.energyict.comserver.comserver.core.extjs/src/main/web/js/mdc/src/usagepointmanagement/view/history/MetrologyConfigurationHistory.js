Ext.define('Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistory', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metrology-configuration-history-tab',

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
                    itemId: 'metrology-configuration-history-grid-id'
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
                            privileges: Mdc.privileges.UsagePoint.canAdmin(),
                            disabled: true
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'metrology-configuration-history-preview',
                    itemId: 'metrology-configuration-history-preview-id'
                }
            }
        ];
        me.callParent(arguments);
    }
});