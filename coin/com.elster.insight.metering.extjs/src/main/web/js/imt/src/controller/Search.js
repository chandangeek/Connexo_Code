Ext.define('Imt.controller.Search', {
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
        'Imt.service.Search'
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
        }
    ],

    lastRequest: undefined,

    init: function () {
        var me = this,
            searchResults = Ext.getStore('Uni.store.search.Results'),
            router = me.getController('Uni.controller.history.Router');

        me.service = Ext.create('Imt.service.Search', {
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
            'uni-view-search-overview button[action=clearFilters]': {
                click: {
                    fn: me.service.clearFilters,
                    scope: me.service
                }
            },
            'uni-view-search-overview button[action=count]': {
                click: {
                    fn: me.service.count,
                    scope: me.service
                }
            }
        });

        searchResults.on('load', function (store, items) {
//            var grid = me.getResultsGrid();
//            var btn = grid.down('#search-bulk-actions-button');
//            btn.setDisabled(!(me.service.searchDomain && me.service.searchDomain.getId() === "com.energyict.mdc.device.data.Device" && items && items.length));
        });
    },

    showOverview: function () {
        var me = this,
            searchDomains = Ext.getStore('Uni.store.search.Domains'),
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.widget('uni-view-search-overview', {
                service: me.service
            });

        me.getApplication().fireEvent('changecontentevent', widget);

        searchDomains.clearFilter(true);
        searchDomains.addFilter({property: 'application', value: 'COIN'});
        searchDomains.load({callback: function(records) {
            var value = router.queryParams.searchDomain,
                selector = me.getObjectSelector(),
                state, isStateChange;

            if (!!router.queryParams.restore === true) {
                me.service.initState();
                state = me.service.getState();
                isStateChange = !!(state && state.domain);
            }

            if (value && !Ext.isEmpty(records) && searchDomains.getById(value) !== null) {
                selector.setValue(value, isStateChange);
            } else if (selector && !Ext.isEmpty(records)) {
                selector.setValue('com.elster.jupiter.metering.UsagePoint', isStateChange);
            }
        }});

        var grid = me.getResultsGrid();

        grid.down('pagingtoolbartop').insert(3, {
            xtype: 'button',
            text: 'Bulk actions',
            itemId: 'search-bulk-actions-button',
            handler: me.showBulkAction,
            privileges: Imt.privileges.UsagePoint.hasBulkActionPrivileges(),
            scope: me
        });

        var listeners = me.service.on({
            change: me.availableClearAll,
            reset: me.availableClearAll,
            scope: me,
            destroyable: true
        });
        widget.on('destroy', function () {
            listeners.destroy();
        }, me);
    },

    showBulkAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('search/bulkaction').forward();
    },


    availableClearAll: function () {
        var me = this,
            searchOverview = me.getSearchOverview(),
            filters = me.service.getFilters();

        searchOverview.down('[action=clearFilters]').setDisabled(!(filters && filters.length));
    }
});
