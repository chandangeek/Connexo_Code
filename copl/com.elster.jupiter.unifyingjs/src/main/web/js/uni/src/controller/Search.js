/**
 * @class Uni.controller.Search
 */
Ext.define('Uni.controller.Search', {
    extend: 'Ext.app.Controller',

    stores: [
        'Uni.store.search.Domains',
        'Uni.store.search.Fields',
        'Uni.store.search.Properties',
        'Uni.store.search.PropertyValues',
        'Uni.store.search.Removables',
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
        'Uni.controller.history.Search',
        'Uni.service.Search'
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

    filterObjectParam: 'filter',
    lastRequest: undefined,

    filters: Ext.create('Ext.util.MixedCollection', false, function (o) {
        return o ? o.dataIndex : null;
    }),

    criteriaMap: {
        'Boolean':      'uni-search-criteria-boolean',
        'Instant':      'uni-search-criteria-datetime',
        'TimeDuration': 'uni-search-criteria-datetime',
        'BigDecimal':   'uni-search-criteria-numeric',
        'Selection':    'uni-search-criteria-selection'
    },

    fieldMap: {
        'Boolean': 'boolean',
        'Long': 'int',
        'String': 'string'
    },

    columnMap: {
        'Long': 'numbercolumn',
        'Date': 'datecolumn',
        'Boolean': 'booleancolumn',
        'DeviceType': 'uni-grid-column-search-devicetype',
        'DeviceConfiguration': 'uni-grid-column-search-deviceconfiguration'
    },

    defaultColumns: {
        'com.energyict.mdc.device.data.Device': ['id', 'mRID', 'serialNumber', 'deviceTypeName', 'deviceConfigurationName'],
        'com.elster.jupiter.metering.UsagePoint' : ['mRID', 'serviceCategory', 'connectionState', 'openIssues']
    },

    searchDomain: null,

    init: function () {
        var me = this;

        me.initHistorians();
        //me.initStoreListeners();

        me.service = Uni.service.Search;
        me.service.setRouter(me.getController('Uni.controller.history.Router'));


        me.control({
            'search-object-selector': {
                change: me.onChangeSearchDomain
            },
            'search-criteria-selector menu menucheckitem': {
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
            }
        });
    },

    /**
     * Forces history controller initialization.
     */
    initHistorians: function () {
        var historian = this.getController('Uni.controller.history.Search');
    },

    showOverview: function () {
        var me = this,
            removablesStore = Ext.getStore('Uni.store.search.Removables'),
            searchDomains = Ext.getStore('Uni.store.search.Domains'),
            router = this.getController('Uni.controller.history.Router'),
            widget = Ext.widget('uni-view-search-overview', {
                service: me.service
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        me.filters.removeAll();

        removablesStore.load();
        searchDomains.load({callback: function(records) {
            var value = router.queryParams.searchDomain,
            selector = me.getObjectSelector();

            if (value && !Ext.isEmpty(records) && searchDomains.findRecord('displayValue', value) !== null) {
                selector.setValue(searchDomains.findRecord('displayValue', value).get('id'));
            } else if (selector && !Ext.isEmpty(records)) {
                selector.setValue(records[0].get('id'));
            }
        }});
    },

    //onBeforeLoad: function (store, options) {
    //    var me = this,
    //        params = {};
    //
    //    options.params = options.params || {};
    //
    //    if (Uni.util.Filters.hasActiveFilter(me.filters)) {
    //        var tempParams = Uni.util.Filters.getFilterParams(me.filters, false, false);
    //        params[me.filterObjectParam] = Uni.util.Filters.createFiltersObject(tempParams);
    //    }
    //
    //    Uni.util.Filters.updateHistoryState(me.filters);
    //
    //    Ext.apply(options.params, params);
    //},


    onChangeSearchDomain: function (field, value) {
        this.service.setDomain(value);
    },

    updateConstraints: function (widget, value) {
        var me = this, store;
        var deps = me.filters.filterBy(function(filter) {
            return !!(filter.property.get('constraints')
            && filter.property.get('constraints').length
            && filter.property.get('constraints').indexOf(widget.property.get('name')) >= 0);
        });

        if (deps.length) {
            deps.each(function(item) {
                if (!Ext.isEmpty(value)) {
                    item.setDisabled(false);
                    if (item.store && Ext.isFunction(item.getStore)) {
                        store = item.getStore();
                        //if (store.isLoading()) {
                        //    Ext.Ajax.abort(store.lastRequest);
                        //}
                        store.clearFilter(true);
                        store.addFilter(widget.getFilter(), false);
                        //item.menu.setLoading(true);
                        //store.load({
                        //    callback: function () {
                        //        item.menu.setLoading(false);
                        //    }
                        //});
                        //store.lastRequest = Ext.Ajax.getLatest();
                    }
                } else {
                    item.setDisabled(true);
                    if (item.store) {
                        item.getStore().clearFilter();
                    }
                }
            });
        }
    }
});