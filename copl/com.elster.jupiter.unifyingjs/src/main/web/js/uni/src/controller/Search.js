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
        'Uni.grid.column.search.DeviceConfiguration'
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
        'Boolean': 'uni-view-search-field-yesno',
        'Instant': 'uni-view-search-field-date-field',
        'TimeDuration': 'uni-view-search-field-date-field',
        'BigDecimal': 'uni-view-search-field-number-field'
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
        'com.elster.jupiter.metering.UsagePoint' : ['mRID', 'serviceCategory', 'name', 'aliasName', 'serviceLocationId', 'createTime']
    },

    searchDomain: null,

    init: function () {
        var me = this;

        me.initHistorians();
        me.initStoreListeners();

        me.control({
            'search-object-selector': {
                change: me.onChangeSearchDomain
            },
            'search-criteria-selector menu menucheckitem': {
                checkchange: me.onCriteriaChange
            },
            'uni-view-search-overview button[action=search]': {
                click: me.applyFilters
            },
            'uni-view-search-overview button[action=clearFilters]': {
                click: me.clearFilters
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
            widget = Ext.widget('uni-view-search-overview'),
            removablesStore = Ext.getStore('Uni.store.search.Removables'),
            searchDomains = Ext.getStore('Uni.store.search.Domains');

        me.getApplication().fireEvent('changecontentevent', widget);

        me.filters.removeAll();

        searchDomains.load();
        removablesStore.load();
    },

    onSearchDomainsLoad: function (records, operation, success) {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            value = router.queryParams.searchDomain,
            selector = me.getObjectSelector();

        if (value && !Ext.isEmpty(records) && records.findRecord('displayValue', value) !== null) {
            selector.setValue(records.findRecord('displayValue', value).get('id'));
        } else if (selector && !Ext.isEmpty(records)) {
            selector.setValue(records.first().get('id'));
        }
    },

    initStoreListeners: function () {
        var me = this,
            searchDomains = Ext.getStore('Uni.store.search.Domains'),
            searchProperties = Ext.getStore('Uni.store.search.Properties');

        searchDomains.on({
            load: me.onSearchDomainsLoad,
            scope: me
        });

        searchProperties.on({
            load: me.onSearchPropertiesLoad,
            scope: me
        });
    },

    onBeforeLoad: function (store, options) {
        var me = this,
            params = {};

        options.params = options.params || {};

        if (Uni.util.Filters.hasActiveFilter(me.filters)) {
            var tempParams = Uni.util.Filters.getFilterParams(me.filters, false, false);
            params[me.filterObjectParam] = Uni.util.Filters.createFiltersObject(tempParams);
        }

        Uni.util.Filters.updateHistoryState(me.filters);

        Ext.apply(options.params, params);
    },

    onSearchPropertiesLoad: function (records, operation, success) {
        var me = this,
            criteriaStore = Ext.getStore('Uni.store.search.Removables');

        criteriaStore.removeAll();

        if (!Ext.isEmpty(records)) {
            records.each(function (record) {
                if (!record.get('sticky')) {
                    criteriaStore.add(record);
                }
            });
        }

        Ext.suspendLayouts();

        me.initStickyCriteria();
        me.initRemovableCriteria();

        me.getCriteriaSelector().bindStore(criteriaStore);
        Uni.util.Filters.loadHistoryState(me.filters, true);

        Ext.resumeLayouts(true);
    },

    initStickyCriteria: function () {
        var me = this,
            propertiesStore = Ext.getStore('Uni.store.search.Properties'),
            container = me.getStickyPropertiesContainer();

        propertiesStore.filter('sticky', true);

        me.removeStickyProperties();
        propertiesStore.each(function (property) {
            me.addProperty(property, container);
        });
        container.setVisible(propertiesStore.count());

        propertiesStore.clearFilter();
    },

    initRemovableCriteria: function () {
        var me = this;
        me.removeRemovableProperties();
    },

    onChangeSearchDomain: function (field, value) {
        var me = this,
            container = me.getSearchOverview(),
            router = this.getController('Uni.controller.history.Router'),
            searchDomains = Ext.getStore('Uni.store.search.Domains'),
            searchResults = Ext.getStore('Uni.store.search.Results'),
            searchProperties = Ext.getStore('Uni.store.search.Properties'),
            fields = Ext.getStore('Uni.store.search.Fields'),
            searchDomain = searchDomains.findRecord('id', value, 0, true, true);

        container.setLoading(true);

        if (searchDomain !== null && Ext.isDefined(searchDomain)) {
            me.searchDomain = searchDomain;
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false); //todo: this is probably a bug un unifying, so this line shouldn't be here
            router.getRoute().forward(null, {searchDomain: searchDomain.get('displayValue')});

            searchProperties.removeAll();
            searchProperties.getProxy().url = searchDomain.get('glossaryHref');
            searchProperties.load({
                callback: function () {
                    container.setLoading(false);
                    fields.removeAll();
                    fields.getProxy().url = searchDomain.get('describedByHref');
                    fields.load({
                        callback: function (records, operation, success) {
                            if (success) {
                                me.updateResultModelAndColumnsFromFields(records);

                                searchResults.removeAll(true);
                                searchResults.clearFilter(true);
                                searchResults.getProxy().url = searchDomain.get('selfHref');
                                searchResults.load();
                            }
                        },
                        scope: me
                    });
                },
                scope: me
            });
        }
    },

    updateResultModelAndColumnsFromFields: function (fields) {
        var me = this,
            grid = me.getResultsGrid(),
            fieldItems = [],
            columnItems = [],
            defaultColumns = me.defaultColumns[me.searchDomain.get('id')];

        fields.forEach(function (field) {
            fieldItems.push(me.createFieldDefinitionFromModel(field));

            if (defaultColumns && defaultColumns.indexOf(field.get('propertyName')) >= 0) {
                columnItems.push(me.createColumnDefinitionFromModel(field));
            }
        });

        grid.store.model.setFields(fieldItems);
        grid.reconfigure(null, columnItems);
    },

    createColumnDefinitionFromModel: function (field) {
        var propertyName = field.get('propertyName'),
            type = this.columnMap[field.get('type')],
            displayValue = field.get('displayValue');

        if (!type) {
            type = 'gridcolumn';
        }

        return {
            dataIndex: propertyName,
            header: displayValue,
            xtype: type
        };
    },

    createFieldDefinitionFromModel: function (field) {
        var propertyName = field.get('propertyName'),
            type = this.fieldMap[field.get('type')];

        if (!type) {
            type = 'auto';
        }

        return {
            name: propertyName,
            type: type
        };
    },

    onCriteriaChange: function (field, checked, e) {
        var me = this,
            container = me.getRemovablePropertiesContainer();

        if (checked) {
            me.addProperty(field.criteria, container, true);
        } else {
            me.removeProperty(field.criteria, container, true);
        }

        container.setVisible(container.items.length);
    },

    removeStickyProperties: function () {
        var me = this,
            property,
            container = me.getStickyPropertiesContainer();

        Ext.suspendLayouts();

        container.removeAll();
        container.setVisible(false);
        me.filters.each(function (filter) {
            property = filter.property;

            if (property.get('sticky')) {
                me.filters.remove(filter);
            }
        });

        Ext.resumeLayouts(true);
    },

    addProperty: function (property, container, removable) {
        var me = this,
            filter = me.createWidgetForProperty(property, removable);

        if (Ext.isDefined(filter)) {
            me.filters.add(filter);
            container.add(filter);
        }
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
    },

    removeProperty: function (property, container, removable) {
        var me = this,
            criteriaSelector = me.getCriteriaSelector(),
            filterResult;

        me.filters.each(function (filter) {
            if (filter.property === property) {
                filterResult = filter;
                return false;
            }
        });

        if (Ext.isDefined(filterResult)) {
            var widget = removable ? filterResult.widget : filterResult;

            widget.reset();

            if (removable) {
                criteriaSelector.setChecked(property, false);
            }

            me.filters.remove(filterResult);
            container.remove(filterResult);

            //Uni.util.Filters.updateHistoryState(me.filters);
        }
    },

    removeRemovableProperties: function () {
        var me = this,
            criteriaSelector = me.getCriteriaSelector(),
            container = me.getRemovablePropertiesContainer(),
            property;

        Ext.suspendLayouts();
        container.removeAll();
        container.setVisible(false);
        me.filters.each(function (filter) {
            property = filter.property;

            if (!property.get('sticky')) {
                me.filters.remove(filter);
                criteriaSelector.setChecked(property, false);
            }
        });

        Ext.resumeLayouts(true);
    },

    clearFilters: function () {
        var me = this;

        me.filters.each(function (filter) {
            filter.reset();
        }, me);

        me.removeRemovableProperties();
        me.applyFilters();
    },

    applyFilters: function () {
        var me = this,
            searchResults = Ext.getStore('Uni.store.search.Results'),
            filters = [];

        me.filters.each(function (item) {
            if (!Ext.isEmpty(item.getValue())) {
                filters.push(item.getFilter())
            }
        });

        searchResults.clearFilter(true);
        searchResults.filter(filters, true);
        searchResults.load();
    },

    createWidgetForProperty: function (property, removable) {
        var me = this,
            type = property.get('type'),
            displayValue = property.get('displayValue'),
            config = {
                xtype: me.criteriaMap[type],
                text: displayValue,
                emptyText: displayValue,
                dataIndex: property.get('name'),
                property: property,
                listeners: {
                    'change': {
                        fn: me.updateConstraints,
                        scope: me
                    }
                }
            },
            widget;

        if (property.get('exhaustive')) {
            var store = Ext.create('Uni.store.search.PropertyValues', {
                proxy: {
                    type: 'ajax',
                    pageParam: undefined,
                    startParam: undefined,
                    limitParam: undefined,
                    url: property.get('linkHref'),
                    reader: {
                        type: 'json',
                        root: 'values'
                    }
                }
            });

            store.load();

            Ext.apply(config, {
                xtype: 'search-combo',
                emptyText: displayValue,
                store: store,
                valueField: 'id',
                displayField: 'displayValue',
                multiSelect: property.get('selectionMode') === 'multiple'
            });
        }

        if (property.get('constraints') && property.get('constraints').length) {
            Ext.apply(config, {
                disabled: true
            });
        }

        if (Ext.isEmpty(config.xtype)){
            Ext.apply(config, {
                xtype: 'search-criteria-simple'
            });
        }

        widget = Ext.widget(config);

        if (removable) {
            widget = Ext.create('Uni.view.search.field.internal.Adapter', {
                widget: widget,
                removeHandler: function () {
                    me.removeProperty(property, me.getRemovablePropertiesContainer(), true);
                }
            });
        }

        return widget;
    }
});