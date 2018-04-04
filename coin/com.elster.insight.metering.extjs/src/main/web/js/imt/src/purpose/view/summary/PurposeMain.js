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
        'Imt.purpose.view.summary.PurposeRegisterDataView',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputReadings'
    ],

    initComponent: function () {
        var me = this,
            router = me.router;

        me.title = router.getRoute().getTitle();
        me.content = [
            {
                xtype: 'tabpanel',
                ui: 'large',
                title: me.title,
                itemId: 'purposeTabPanel',
                activeTab: 'purpose-' + me.tab,
                items: [
                    me.getOverviewComponent(),
                    me.getIntervalDataViewComponent(),
                    me.getRegisterDataViewComponent()
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
    },

    getOverviewComponent: function () {
        var me = this;
        return {
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
        }
    },


    getIntervalDataViewComponent: function () {
        var me = this,
            intervalDataViewComponent,
            noIntervalOutputsComponent;

        intervalDataViewComponent = {
            title: Uni.I18n.translate('purpose.summary.dataView', 'IMT', 'Interval data view'),
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
        };

        noIntervalOutputsComponent = {
            title: Uni.I18n.translate('purpose.summary.dataView', 'IMT', 'Interval data view'),
            itemId: 'purpose-data-view',
            items: {
                xtype: 'uni-form-empty-message',
                itemId: 'purpose-data-view-empty-message',
                text: Uni.I18n.translate('purpose.summary.dataView.no.interval.outputs', 'IMT', 'No interval outputs on this purpose')
            }
        };

        return me.intervalsCount ? intervalDataViewComponent : noIntervalOutputsComponent;
    },


    getRegisterDataViewComponent: function () {
        var me = this,
            registerDataViewComponent,
            noRegisterOutputsComponent;

        registerDataViewComponent = {
            title: Uni.I18n.translate('purpose.summary.registerDataView', 'IMT', 'Register data view'),
            itemId: 'purpose-register-data-view',
            items: {
                xtype: 'purpose-register-data-view',
                purpose: me.purpose,
                usagePoint: me.usagePoint,
                router: me.router,
                outputs: me.outputs,
                prevNextListLink: me.prevNextListLink
            },
            listeners: {
                activate: me.controller.showRegisterDataViewTab,
                scope: me.controller
            },
            usagePoint: me.usagePoint,
            purpose: me.purpose
        };

        noRegisterOutputsComponent = {
            title: Uni.I18n.translate('purpose.summary.registerDataView', 'IMT', 'Register data view'),
            itemId: 'purpose-register-data-view',
            items: {
                xtype: 'uni-form-empty-message',
                itemId: 'purpose-data-view-empty-message',
                text: Uni.I18n.translate('purpose.summary.dataView.no.regsiter.outputs', 'IMT', 'No register outputs on this purpose')
            }
        };
        return me.registersCount ? registerDataViewComponent : noRegisterOutputsComponent;
    }

});
