/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecalls.controller.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.servicecalls.view.ServiceCallsSetup'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint'
    ],

    stores: [
        'Scs.store.object.ServiceCallHistory',
        'Scs.store.object.RunningServiceCalls',
        'Scs.store.ServiceCallTypes',
        'Scs.store.States'
    ],

    requires: [
        'Uni.controller.Navigation',
        'Uni.util.History'
    ],

    refs: [
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
        },
        {
            ref: 'filter',
            selector: 'service-call-filter'
        },
        {
            ref: 'serviceCallsSetup',
            selector: 'service-calls-setup'
        },
        {
            ref: 'runningServiceCallsGrid',
            selector: '#running-service-calls-grid'
        }
    ],

    historyAdded: false,
    skip: false,

    init: function () {
        this.control({
            'service-calls-setup #object-service-calls-tab-panel': {
                tabchange: this.onTabChange
            },
            '#object-service-calls-action-menu': {
                click: this.chooseAction
            },
            'cancel-all-action-menu': {
                click: this.cancelAllAction
            }
        });
    },

    showServiceCalls: function (usagePointId) {
        var me = this;

        if (!Uni.util.History.isSuspended()) {
            me.showTabbedView(usagePointId, 0);
        }

    },

    showServiceCallHistory: function (usagePointId) {
        var me = this;
        if (!Uni.util.History.isSuspended() && !me.skip) {
            me.showTabbedView(usagePointId, 1);
        } else if (!me.historyAdded) {
            me.skip = true;
            Ext.getStore('Scs.store.object.ServiceCallHistory').getProxy().setUrl('/api/udr/usagepoints/' + encodeURIComponent(usagePointId) + '/servicecallhistory');
            me.getServiceCallsSetup().addHistoryGrid(me.getSixtyDaysFilter());
            me.historyAdded = true;
        } else {
            me.skip = false;
        }

    },

    showTabbedView: function (usagePointId, activeTab) {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            router = me.getController('Uni.controller.history.Router'),
            widget,
            historyStore = Ext.getStore('Scs.store.object.ServiceCallHistory'),
            runningStore = Ext.getStore('Scs.store.object.RunningServiceCalls'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View');

        viewport.setLoading();
        me.historyAdded = false;
        runningStore.getProxy().setUrl('/api/udr/usagepoints/' + encodeURIComponent(usagePointId) + '/runningservicecalls');

        usagePointsController.loadUsagePoint(usagePointId, {
            success: function (types, usagepoint) {
                widget = Ext.widget('service-calls-setup', {
                    usagePointId: usagePointId,
                    usagePoint: usagepoint,
                    router: router,
                    activeTab: activeTab
                });
                if (activeTab === 1) {
                    historyStore.getProxy().setUrl('/api/udr/usagepoints/' + encodeURIComponent(usagePointId) + '/servicecallhistory');
                    widget.addHistoryGrid(me.getSixtyDaysFilter());
                    me.historyAdded = true;
                }
                me.getApplication().fireEvent('usagePointLoaded', usagepoint);
                me.getApplication().fireEvent('changecontentevent', widget);
                viewport.setLoading(false);
            },
            failure: function () {
                viewport.setLoading(false);
            }
        });
    },

    onTabChange: function (tabPanel, newTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route,
            filter = {};

        router.arguments.usagePointId = tabPanel.usagePointId;
        Uni.util.History.suspendEventsForNextCall(true);
        if (newTab.itemId === 'history-service-calls-tab') {
            if (me.getFilter()) {
                filter = me.getFilter().getFilterParams(false, true);
            }
            route = router.getRoute('usagepoints/view/servicecalls/history');
        } else if (newTab.itemId === 'running-service-calls-tab') {
            route = router.getRoute('usagepoints/view/servicecalls');
        }
        route.forward({}, filter);
    },

    getSixtyDaysFilter: function () {
        var me = this,
            date,
            filter = {};

        filter.toDate = me.getTomorrow();

        date = me.getTomorrow();
        date.setDate(date.getDate() - 60);
        filter.fromDate = date;

        return filter;
    },

    getTomorrow: function () {
        var date = new Date();
        date.setDate(date.getDate() + 1);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        date.setMilliseconds(0);

        return date;
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'cancel':
                me.cancelServiceCall(menu.record);
        }
    },

    cancelServiceCall: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.yes', 'IMT', 'Yes'),
                cancelText: Uni.I18n.translate('general.no', 'IMT', 'No')
            }),
            store = Ext.getStore('Scs.store.object.RunningServiceCalls'),
            serviceCallState = record.get('state'),
            router = me.getController('Uni.controller.history.Router');

        record.setProxy(
            {
                type: 'rest',
                url: '/api/udr/usagepoints/' + encodeURIComponent(router.arguments.usagePointId) + '/runningservicecalls',
                timeout: 120000,
                reader: {
                    type: 'json'
                }
            });
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('usagepoint.servicecall.remove.msg', 'IMT', 'This service call will no longer be running. Do you wish to continue?'),
                title: Uni.I18n.translate('general.cancelX', 'IMT', "Cancel '{0}'?", [record.data.name]),
                fn: function (state) {
                    if (state === 'confirm') {
                        serviceCallState.id = "sclc.default.cancelled";
                        record.set('state', serviceCallState);
                        if (record.get('parents') === '') {
                            record.set('parents', [])
                        }
                        if (record.get('children') === '') {
                            record.set('children', [])
                        }
                        if (record.get('targetObject') === '') {
                            record.set('targetObject', null)
                        }
                        record.save({
                            success: function (newRecord) {
                                store.load();
                            }
                        });
                    }
                }
            });
    },

    cancelAllAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'cancel-all':
                me.cancelAllServiceCalls();
        }
    },

    cancelAllServiceCalls: function () {
        var me = this,
            serviceCallState = {id: 'sclc.default.cancelled'},
            store = Ext.getStore('Scs.store.object.RunningServiceCalls'),
            router = me.getController('Uni.controller.history.Router'),
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.yes', 'IMT', 'Yes'),
                cancelText: Uni.I18n.translate('general.no', 'IMT', 'No')
            });

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('useagepoint.servicecall.cancelall.msg', 'IMT', 'All the service calls that can be cancelled will be cancelled. Do you wish to continue?'),
                title: Uni.I18n.translate('usagepoint.servicecall.cancelall.title', 'IMT', "Cancel all ongoing service calls?"),
                fn: function (state) {
                    if (state === 'confirm') {
                        me.getRunningServiceCallsGrid().setLoading(true);
                        Ext.Ajax.request({
                            url: '/api/udr/usagepoints/' + encodeURIComponent(router.arguments.usagePointId) + '/servicecalls',
                            jsonData: {state: serviceCallState},
                            method: 'PUT',
                            success: function () {
                                me.getRunningServiceCallsGrid().setLoading(false);
                                store.load();
                            },
                            failure: function (response, opts) {
                                me.getRunningServiceCallsGrid().setLoading(false);
                            }
                        })
                    }
                }
            });
    }
});

