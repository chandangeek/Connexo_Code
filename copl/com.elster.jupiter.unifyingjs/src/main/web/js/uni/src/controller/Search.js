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
        'Uni.view.search.Adapter',
        'Uni.util.Filters',
        'Uni.view.search.field.Combobox',
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
            }

            //removablePropertiesContainer.on({
            //    add: me.updateRemovableContainerVisibility,
            //    remove: me.updateRemovableContainerVisibility,
            //    scope: me
            //});
            //
            //searchButton.on({
            //    click: me.applyFilters,
            //    scope: me
            //});
            //
            //clearFiltersButton.on({
            //    click: me.clearFilters,
            //    scope: me
            //});
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
            searchProperties = Ext.getStore('Uni.store.search.Properties'),
            removableProperties = Ext.getStore('Uni.store.search.Removables'),
            resultsStore = Ext.getStore('Uni.store.search.Results');

        searchDomains.on({
            load: me.onSearchDomainsLoad,
            scope: me
        });

        searchProperties.on({
            load: me.onSearchPropertiesLoad,
            scope: me
        });

        //resultsStore.on({
        //    beforeload: me.onBeforeLoad,
        //    scope: me
        //});
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

    //updateRemovableContainerVisibility: function () {
    //    var me = this,
    //        container = me.getRemovablePropertiesContainer(),
    //        itemCount = container.items.length,
    //        placeholder = me.getRemovablePropertiesPlaceholder();
    //
    //    placeholder.setVisible(itemCount > 0);
    //    container.setVisible(itemCount > 0);
    //},

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
        //me.applyFilters();

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
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false); //todo: this is probably a bug un unifying, so this line shouldn't be here
            router.getRoute().forward(null, {searchDomain: searchDomain.get('displayValue')});

            searchProperties.removeAll();
            searchProperties.getProxy().url = searchDomain.get('glossaryHref');
            searchProperties.load({
                callback: function () {
                    container.setLoading(false);
                },
                scope: me
            });

            fields.removeAll();
            fields.getProxy().url = searchDomain.get('describedByHref');
            fields.load({
                callback: function (records, operation, success) {
                    if (success) {
                        //me.updateResultModelAndColumnsFromFields(records);
                        //
                        //searchResults.removeAll();
                        //searchResults.getProxy().url = searchDomain.get('selfHref');
                        //me.lastRequest = searchResults.load();
                    }
                },
                scope: me
            });
        }
    },

    updateResultModelAndColumnsFromFields: function (fields) {
        var me = this,
            grid = me.getResultsGrid(),
            fieldItems = [],
            columnItems = [];

        fields.forEach(function (field) {
            fieldItems.push(me.createFieldDefinitionFromModel(field));
            columnItems.push(me.createColumnDefinitionFromModel(field));
        });

        grid.store.model.setFields(fieldItems);
        grid.reconfigure(null, columnItems);
    },

    createColumnDefinitionFromModel: function (field) {
        var propertyName = field.get('propertyName'),
            type = field.get('type'),
            displayValue = field.get('displayValue'),
            columnType = 'gridcolumn';

        switch (type) {
            case 'Long':
                columnType = 'numbercolumn';
                break;
            case 'Date':
                columnType = 'datecolumn';
                break;
            case 'Boolean':
                columnType = 'booleancolumn';
                break;
            // Custom grid columns.
            case 'DeviceType':
                columnType = 'uni-grid-column-search-devicetype';
                break;
            case 'DeviceConfiguration':
                columnType = 'uni-grid-column-search-deviceconfiguration';
                break;
        }

        return {
            dataIndex: propertyName,
            header: displayValue,
            xtype: columnType
        };
    },

    createFieldDefinitionFromModel: function (field) {
        var propertyName = field.get('propertyName'),
            type = field.get('type');

        switch (type) {
            case 'Boolean':
                type = 'boolean';
                break;
            case 'Long':
                type = 'int';
                break;
            case 'String':
                type = 'string';
                break;
            default:
                type = 'auto';
                break;
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
            filter = me.createWidgetForProperty(property, removable),
            constraints = property.get('constraints'),
            widget = removable ? filter.widget : filter;

        if (Ext.isDefined(filter)) {
            me.filters.add(filter);
            container.add(filter);

            widget.on('filterupdate', me.applyFilters, me);
            //me.applyConstraintListeners(property, widget);
            //me.updateRemovableContainerVisibility();
        }
    },

    applyConstraintListeners: function (property, widget) {
        var me = this,
            constraints,
            removable;

        me.filters.each(function (filter) {
            constraints = filter.property.get('constraints');
            removable = !filter.property.get('sticky');

            if (Ext.isArray(constraints) && !Ext.isEmpty(constraints)) {
                for (var i = 0; i < constraints.length; i++) {
                    if (property.get('name') === constraints[i]) {
                        widget.on('change', function () {
                            me.updateConstraints(removable ? filter.widget : filter, property.get('name'), widget.getParamValue());
                        }, me);
                    }
                }
            }
        });

        constraints = property.get('constraints');
        removable = !property.get('sticky');

        for (var i = 0; i < constraints.length; i++) {
            me.filters.each(function (filter) {
                if (filter.property.get('name') === constraints[i]) {
                    if (removable) {
                        filter.widget.on('change', function () {
                            me.updateConstraints(widget, filter.property.get('name'), filter.widget.getParamValue());
                        }, me);
                    } else {
                        filter.on('change', function () {
                            me.updateConstraints(widget, filter.property.get('name'), filter.getParamValue());
                        }, me);
                    }
                }
            });
        }
    },

    updateConstraints: function (widget, filterName, filterValues) {
        var store = widget.store;

        if (Ext.isDefined(store)) {
            if (!Ext.isEmpty(filterValues)) {
                store.filter(filterName, filterValues);
            } else {
                store.clearFilter();
            }
        }
    },

    removeProperty: function (property, container, removable) {
        var me = this,
            criteriaSelector = me.getCriteriaSelector(),
            filterResult = undefined;

        me.filters.each(function (filter) {
            if (filter.property === property) {
                filterResult = filter;
                return false;
            }
        });

        if (Ext.isDefined(filterResult)) {
            var widget = removable ? filterResult.widget : filterResult;

            widget.resetValue();

            if (removable) {
                criteriaSelector.setChecked(property, false);
            }

            me.filters.remove(filterResult);
            container.remove(filterResult);

            //Uni.util.Filters.updateHistoryState(me.filters);
            //me.updateRemovableContainerVisibility();
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
            filter.resetValue();
        }, me);

        me.removeRemovableProperties();
        me.applyFilters();
    },

    applyFilters: function () {
        var me = this,
            searchResults = Ext.getStore('Uni.store.search.Results');

        searchResults.removeAll();

        if (me.lastRequest) {
            Ext.Ajax.abort(me.lastRequest);
        }

        try {
            me.lastRequest = searchResults.load();
        } catch (ex) {
            // Ignore the 'indexOf' exception caused by interrupted calls.
        }
    },

    createWidgetForProperty: function (property, removable) {
        var me = this,
            type = property.get('type'),
            displayValue = property.get('displayValue'),
            widget = undefined;

        if (property.get('exhaustive')) {
            var store = Ext.create('Uni.store.search.PropertyValues', {
                pageSize: 10,
                proxy: {
                    type: 'ajax',
                    //pageParam: undefined,
                    //startParam: undefined,
                    //limitParam: undefined,
                    url: property.get('linkHref'),
                    reader: {
                        type: 'json',
                        root: 'values'
                    }
                }
            });

            widget = Ext.create('Uni.view.search.field.Combobox', {
                emptyText: displayValue,
                store: store,
                valueField: 'id',
                displayField: 'displayValue',
                multiSelect: true
            });

            //widget.on('afterrender', function () {
                store.load();
            //}, me);
        } else {
            switch (type) {
                case 'String':
                    widget = Ext.create('Uni.view.search.field.Simple', {
                        text: displayValue,
                        emptyText: displayValue
                    });
                    break;
                case 'BigDecimal':
                    widget = Ext.create('Uni.grid.filtertop.Number', {
                        emptyText: displayValue
                    });
                    break;
                case 'Boolean':
                    widget = Ext.create('Uni.view.search.field.YesNo', {
                        emptyText: displayValue
                    });
                    break;
                default:
                    // <debug>
                    console.log('Unknown search property type: ' + type);
                    // </debug>
                    return undefined;
                    break;
            }
        }

        Ext.apply(widget, {
            dataIndex: property.get('name'),
            property: property
        });

        if (removable) {
            widget = Ext.create('Uni.view.search.Adapter', {
                widget: widget,
                removeHandler: function () {
                    me.removeProperty(property, me.getRemovablePropertiesContainer(), true);
                }
            });
        }

        return widget;
    }
});