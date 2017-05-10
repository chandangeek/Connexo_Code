/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.PurposeMain', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.purpose-main',
    itemId: 'tabbedPurposeView',
    requires: [
        'Uni.view.toolbar.PreviousNextNavigation',
        'Imt.purpose.view.summary.PurposeDataView',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputReadings'
    ],

    initComponent: function () {
        var me = this,
            router = me.router,
            dataStore,
            dataViewComponent;

        dataViewComponent = me.intervalsCount ? {
            title:  Uni.I18n.translate('purpose.summary.dataView', 'IMT', 'Data view'),
            itemId: 'purpose-data-view',
            items: {
                xtype: 'purpose-data-view',
                interval: me.interval,
                purpose: me.purpose,
                usagePoint: me.usagePoint,
                outputs: me.outputs,
                router: me.router,
                prevNextListLink: me.prevNextListLink
            },
            listeners: {
                activate: me.controller.showDataViewTab,
                scope: me.controller
            },
            usagePoint: me.usagePoint,
            purpose: me.purpose,
            output: me.output

        } : {
            title:  Uni.I18n.translate('purpose.summary.dataView', 'IMT', 'Data view'),
            itemId: 'purpose-data-view',
            items: {
                xtype: 'uni-form-empty-message',
                itemId: 'purpose-data-view-empty-message',
                text: Uni.I18n.translate('purpose.summary.dataView.no.interval.outputs', 'IMT', 'No interval outputs on this purpose')
            }
        };

        me.title = router.getRoute().getTitle();
        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                title: router.getRoute().getTitle(),
                itemId: 'purposeTabPanel',
                activeTab: 'purpose-' + me.tab,
                items: [
                    {
                        title: Uni.I18n.translate('purpose.summary.overview', 'IMT', 'Overview'),
                        itemId: 'purpose-overview',
                        items: {
                            xtype: 'purpose-outputs',
                            router: me.router,
                            usagePoint: me.usagePoint,
                            purposes: me.purposes,
                            purpose: me.purpose,
                            defaultPeriod: me.defaultPeriod
                        },
                        listeners: {
                            activate: me.controller.showOverviewTab,
                            scope: me.controller
                        },
                        purpose: me.purpose
                    },
                    dataViewComponent
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
