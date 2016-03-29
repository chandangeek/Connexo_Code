Ext.define('Mdc.controller.setup.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.servicecalls.ServiceCallsSetup'
    ],

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.servicecalls.ServiceCallHistory',
        'Mdc.store.servicecalls.RunningServiceCalls',
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
            'service-calls-setup #device-service-calls-tab-panel': {
                tabchange: this.onTabChange
            },
            '#device-service-calls-action-menu': {
                click: this.chooseAction
            },
            'cancel-all-action-menu': {
                click: this.cancelAllAction
            }
        });
    },

    showServiceCalls: function (mRID) {
        var me = this;

        if (!Uni.util.History.isSuspended()) {
            me.showTabbedView(mRID, 0);
        }

    },

    showServiceCallHistory: function (mRID) {
        var me = this;
        if (!Uni.util.History.isSuspended() && !me.skip) {
            me.showTabbedView(mRID, 1);
        } else if (!me.historyAdded) {
            me.skip = true;
            Ext.getStore('Mdc.store.servicecalls.ServiceCallHistory').getProxy().setUrl(mRID);
            me.getServiceCallsSetup().addHistoryGrid(me.getSixtyDaysFilter());
            me.historyAdded = true;
        } else {
            me.skip = false;
        }

    },

    showTabbedView: function (mRID, activeTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget,
            historyStore = Ext.getStore('Mdc.store.servicecalls.ServiceCallHistory'),
            runningStore = Ext.getStore('Mdc.store.servicecalls.RunningServiceCalls');

        me.historyAdded = false;
        runningStore.getProxy().setUrl(mRID);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('service-calls-setup', {
                    device: device,
                    router: router,
                    activeTab: activeTab
                });
                if(activeTab === 1) {
                    historyStore.getProxy().setUrl(mRID);
                    widget.addHistoryGrid(me.getSixtyDaysFilter());
                    me.historyAdded = true;
                }
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    onTabChange: function (tabPanel, newTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route,
            filter = {};

        router.arguments.mRID = tabPanel.mRID;
        Uni.util.History.suspendEventsForNextCall(true);
        if (newTab.itemId === 'history-service-calls-tab') {
            if(me.getFilter()) {
                filter = me.getFilter().getFilterParams(false, true);
            }
            route = router.getRoute('devices/device/servicecalls/history');
        } else if (newTab.itemId === 'running-service-calls-tab') {
            route = router.getRoute('devices/device/servicecalls');
        }
        route.forward({}, filter);
    },

    getSixtyDaysFilter: function() {
        var date,
            filter = {};

        date = new Date();
        date.setDate(date.getDate() - 60);
        filter.fromDate = date;
        date = new Date();
        filter.toDate = date;

        return filter;
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
                confirmText: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                cancelText: Uni.I18n.translate('general.no', 'MDC', 'No')
            }),
            store = Ext.getStore('Mdc.store.servicecalls.RunningServiceCalls'),
            serviceCallState = record.get('state'),
            router = me.getController('Uni.controller.history.Router');

        record.setProxy(
            {
                type: 'rest',
                url: '/api/ddr/devices/' + encodeURIComponent(router.arguments.mRID) + '/runningservicecalls',
                timeout: 120000,
                reader: {
                    type: 'json'
                }
            });
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('device.servicecall.remove.msg', 'MDC', 'This service call will be canceled and no longer be running. Do you wish to continue?'),
                title: Uni.I18n.translate('general.cancelX', 'MDC', "Cancel '{0}'?", [record.data.name]),
                fn: function (state) {
                    if (state === 'confirm') {
                        serviceCallState.id = "sclc.default.cancelled";
                        record.set('state', serviceCallState);
                        if(record.get('parents') === '') {
                            record.set('parents', [])
                        }
                        if(record.get('children') === '') {
                            record.set('children', [])
                        }
                        if(record.get('targetObject') === '') {
                            record.set('targetObject', null)
                        }
                        record.save({
                            success: function(newRecord){
                                store.load();
                            }
                        });
                    }
                }
            });
    },

    cancelAllAction: function(menu, item) {
        var me = this;
        switch (item.action) {
            case 'cancel-all':
                me.cancelAllServiceCalls();
        }
    },

    cancelAllServiceCalls: function() {
        var me = this,
            serviceCallState = {id: 'sclc.default.cancelled'},
            store = Ext.getStore('Mdc.store.servicecalls.RunningServiceCalls'),
            router = me.getController('Uni.controller.history.Router'),
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.yes', 'MDC', 'Yes'),
                cancelText: Uni.I18n.translate('general.no', 'MDC', 'No')
            });

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('device.servicecall.cancelall.msg', 'MDC', 'All the service calls that can be cancelled will be cancelled. Do you wish to continue?'),
                title: Uni.I18n.translate('device.servicecall.cancelall.title', 'MDC', "Cancel all ongoing service calls?"),
                fn: function (state) {
                    if (state === 'confirm') {
                        me.getRunningServiceCallsGrid().setLoading(true);
                        Ext.Ajax.request({
                            url: '/api/ddr/devices/' + encodeURIComponent(router.arguments.mRID) + '/servicecalls',
                            jsonData: {state: serviceCallState},
                            method: 'PUT',
                            success: function () {
                                me.getRunningServiceCallsGrid().setLoading(false);
                                store.load();
                            },
                            failure: function(response, opts){
                                me.getRunningServiceCallsGrid().setLoading(false);
                            }
                        })
                    }
                }
            });
    }
});

