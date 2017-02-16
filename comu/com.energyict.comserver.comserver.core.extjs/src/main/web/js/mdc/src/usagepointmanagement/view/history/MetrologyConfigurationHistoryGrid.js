/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metrology-configuration-history-grid',
    // overflowY: 'auto',
    router: null,
    usagePoint: null,
    requires: [
        'Uni.grid.column.Action',
        'Mdc.usagepointmanagement.store.MetrologyConfigurationVersions',
        'Mdc.usagepointmanagement.view.history.MetrologyConfigurationActionMenu'
    ],
    store: 'Mdc.usagepointmanagement.store.MetrologyConfigurationVersions',
    
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                xtype: 'toolbar',
                itemId: 'metrology-configuration-history-toolbar-id',
                dock: 'top',
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'versions-count',
                        width: 150

                    },
                    '->',
                    {
                        xtype: 'button',
                        style: {
                            'background-color': '#71adc7'
                        },
                        itemId: 'add-version-btn',
                        privileges: Mdc.privileges.UsagePoint.canAdmin(),
                        href: me.router.getRoute('usagepoints/usagepoint/history/addmetrologyconfigurationversion').buildUrl(),

                        text: Uni.I18n.translate('usagePoint.generalAttributes.addVersion', 'MDC', 'Add version')
                    }
                ]
            }
        ];

        me.columns = [
            {
                header: Uni.I18n.translate('usagePoint.generalAttributes.period', 'MDC', 'Period'),
                dataIndex: 'period',
                flex: 4
            },
            {
                header: Uni.I18n.translate('usagePoint.generalAttributes.metrologyConfiguration', 'MDC', 'Metrology configuration'),
                dataIndex: 'name',
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.UsagePoint.canAdmin(),
                menu: {
                    xtype: 'metrology-configuration-versions-action-menu',
                    usagePoint: me.usagePoint,
                    itemId: 'metrology-configuration-versions-action-menu-id'
                },
                flex: 1
            }
        ];

        me.callParent(arguments);
    },

    setVersionCount: function (count) {
        var me = this,
            label = Uni.I18n.translatePlural(
                'usagePoint.generalAttributes.versionCount', count, 'MDC',
                'No versions', '{0} version', '{0} versions'
            );
        me.down('#versions-count').setValue(label);
    }
});

