/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class MdcApp.controller.Main
 */
Ext.define('Mdc.controller.Search', {
    extend: 'Ext.app.Controller',

    stores: [
        'Uni.store.search.Domains',
        'Uni.store.search.Fields',
        'Uni.store.search.Properties',
        'Uni.store.search.PropertyValues',
        'Uni.store.search.Results'
    ],

    requires: [
        'Uni.view.search.Overview',
        'Uni.view.search.field.internal.Adapter',
        'Uni.util.Filters',
        'Uni.view.search.field.Selection',
        'Uni.view.search.field.Simple',
        'Uni.grid.column.search.DeviceType',
        'Uni.grid.column.search.DeviceConfiguration',
        'Uni.grid.column.search.Quantity',
        'Uni.grid.column.search.Boolean',
        'Mdc.service.Search'
    ],

    refs: [
        {
            ref: 'searchOverview',
            selector: 'uni-view-search-overview'
        },
        {
            ref: 'objectSelector',
            selector: 'uni-view-search-overview search-object-selector'
        },
        {
            ref: 'criteriaSelector',
            selector: 'uni-view-search-overview search-criteria-selector'
        },
        {
            ref: 'stickyPropertiesContainer',
            selector: 'uni-view-search-overview #search-criteria-sticky'
        },
        {
            ref: 'removablePropertiesContainer',
            selector: 'uni-view-search-overview #search-criteria-removable'
        },
        {
            ref: 'searchButton',
            selector: 'uni-view-search-overview button[action=search]'
        },
        {
            ref: 'clearFiltersButton',
            selector: 'uni-view-search-overview button[action=clearFilters]'
        },
        {
            ref: 'resultsGrid',
            selector: 'uni-view-search-overview uni-view-search-results'
        },
        {
            ref: 'loadButton',
            selector: 'uni-view-search-overview #load-button'
        },
        {
            ref: 'saveSearchButton',
            selector: 'uni-view-search-overview button[action=saveSearchWindow]'
        }
    ],

    lastRequest: undefined,

    init: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.service = Ext.create('Mdc.service.Search', {
            router: router
        });

        me.control({
            'search-object-selector': {
                change: function (field, value) {
                    Uni.util.History.setParsePath(false);
                    router.getRoute('search').forward(null, Ext.apply(router.queryParams, {restore: true}));
                    me.service.setDomain(value);
                }
            },
            'uni-view-search-overview search-criteria-selector menu menucheckitem': {
                checkchange: function(field, checked) {
                    checked
                        ? me.service.addProperty(field.criteria)
                        : me.service.removeProperty(field.criteria);
                }
            },
            'uni-view-search-overview button[action=search]': {
                click: {
                    fn: me.service.applyFilters,
                    scope: me.service
                }
            },
            'uni-view-search-overview button[action=saveSearchWindow]': {
                click: function(){
                    me.service.openSaveSearch(me);
                    scope: me.service

                }
            },
            'uni-view-search-overview #load-button': {
                select: function (combo, value, a){
                    me.service.loadSearch(combo, value, a, me);
                    scope: me.service;
                }

            },
            'uni-view-search-overview button[action=count]': {
                click: {
                    fn: me.service.count,
                    scope: me.service
                }
            },
            'uni-view-search-overview button[action=clearFilters]': {
                click: {
                    fn: me.service.clearFilters,
                    scope: me.service
                }
            }
        });
    },

    showOverview: function () {
        var me = this,
            searchDomains = Ext.getStore('Uni.store.search.Domains'),
            searchResults = Ext.getStore('Uni.store.search.Results'),
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.widget('uni-view-search-overview', {
                service: me.service
            });

        searchResults.on('sort', function () {
            me.getResultsGrid().setLoading(false);
        });
        searchResults.on('beforesort', function () {
            me.getResultsGrid().setLoading(true);
        });

        Ext.suspendLayouts();
        me.getApplication().fireEvent('changecontentevent', widget);

        if (searchDomains.isLoading() && searchDomains.lastRequest) {
            Ext.Ajax.suspendEvent('requestexception');
            Ext.Ajax.abort(searchDomains.lastRequest);
            Ext.Ajax.resumeEvent('requestexception');
        }
        searchDomains.clearFilter(true);
        searchDomains.addFilter({property: 'application', value: 'COMU'}, false);
        searchDomains.load({callback: function(records, op, success) {
            if (widget.rendered && success) {
                var value = router.queryParams.searchDomain,
                    selector = me.getObjectSelector(),
                    state, isStateChange;

                if (!!router.queryParams.restore === true) {
                    me.service.initState();
                    state = me.service.getState();
                    isStateChange = !!(state && state.domain);
                }

                if (!isStateChange) {
                    if (value && !Ext.isEmpty(records) && searchDomains.getById(value) !== null) {
                        me.service.setDomain(searchDomains.getById(value));
                    } else if (selector && !Ext.isEmpty(records)) {
                        me.service.setDomain(records[0]);
                    }
                }
            }
        }});

        searchDomains.lastRequest = Ext.Ajax.getLatest();

        var grid = me.getResultsGrid();
        grid.down('pagingtoolbartop').insert(3, {
            xtype: 'button',
            hidden: !Mdc.privileges.Device.canAadministrateDeviceOrDeviceCommunication(),
            text: Uni.I18n.translate('general.bulkAction', 'MDC', 'Bulk action'),
            itemId: 'search-bulk-actions-button',
            handler: me.showBulkAction,
            scope: me
        });

        grid.down('pagingtoolbartop').insert(1, {
            xtype: 'uni-form-info-message',
            itemId: 'information-message-search-overview',
            text: Uni.I18n.translate('search.overview.informationMessage', 'MDC', "Attention: the search result only allows to sort the columns on the active page. All subsequent pages will not be sorted."),
            margin: '0 10 5 0',
            iconCmp: {
                xtype: 'component',
                style: 'font-size: 22px; color: #71adc7; margin: 0px -22px 0px -22px;',
                cls: 'icon-info'
            },
            width: 720,
            height: 30,
            style: 'border: 1px solid #71adc7; border-radius: 10px; padding: 3px 0px 5px 32px;',
        });

        Ext.resumeLayouts(true);

        var listeners = me.service.on({
            change: me.availableClearAll,
            reset: me.availableClearAll,
            scope: me,
            destroyable: true
        });

        var storeListeners = searchResults.on('load', function (store, items) {
            var btn = grid.down('#search-bulk-actions-button');
            btn.setDisabled(!(me.service.searchDomain && me.service.searchDomain.getId() === "com.energyict.mdc.common.device.data.Device" && items && items.length));
        }, this, {destroyable: true});

        widget.on('destroy', function () {
            listeners.destroy();
            storeListeners.destroy();
        }, me)
    },

    showBulkAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('search/bulkAction').forward();
    },

    availableClearAll: function () {
        var me = this,
            searchOverview = me.getSearchOverview(),
            filters = me.service.getFilters();

        searchOverview.down('[action=clearFilters]').setDisabled(!(filters && filters.length));
        searchOverview.down('[action=saveSearchWindow]').setDisabled(!(filters && filters.length))
    }
});
