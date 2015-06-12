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
        'Uni.util.Filters'
    ],

    refs: [
        {
            ref: 'searchOverview',
            selector: 'uni-view-search-overview'
        },
        {
            ref: 'searchDomainCombo',
            selector: 'uni-view-search-overview combo#domain'
        },
        {
            ref: 'addCriteriaCombo',
            selector: 'uni-view-search-overview combo#addcriteria'
        },
        {
            ref: 'stickyPropertiesContainer',
            selector: 'uni-view-search-overview container#stickycriteria'
        },
        {
            ref: 'removablePropertiesPlaceholder',
            selector: 'uni-view-search-overview component#removablecriteriaplaceholder'
        },
        {
            ref: 'removablePropertiesContainer',
            selector: 'uni-view-search-overview container#removablecriteria'
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
            searchDomains = Ext.getStore('Uni.store.search.Domains');

        me.getApplication().fireEvent('changecontentevent', widget);

        me.initComponentListeners();
        me.filters.removeAll();

        searchDomains.load();
    },

    onSearchDomainsLoad: function (records, operation, success) {
        var me = this,
            searchDomainValue = me.getSearchDomainHistoryValue(),
            searchDomainCombo = me.getSearchDomainCombo();

        if (!Ext.isEmpty(records) && records.findRecord('displayValue', searchDomainValue) !== null) {
            searchDomainCombo.setValue(records.findRecord('displayValue', searchDomainValue).get('id'));
        } else if (searchDomainCombo.getValue() === null && !Ext.isEmpty(records)) {
            searchDomainCombo.setValue(records.first().get('id'));
        }
    },

    initComponentListeners: function () {
        var me = this,
            searchDomainCombo = me.getSearchDomainCombo(),
            addCriteriaCombo = me.getAddCriteriaCombo(),
            removablePropertiesContainer = me.getRemovablePropertiesContainer(),
            searchButton = me.getSearchButton(),
            clearFiltersButton = me.getClearFiltersButton();

        searchDomainCombo.on({
            change: me.onChangeSearchDomain,
            scope: me
        });

        addCriteriaCombo.on({
            select: me.onSelectAddCriteria,
            scope: me
        });

        removablePropertiesContainer.on({
            add: me.updateRemovableContainerVisibility,
            remove: me.updateRemovableContainerVisibility,
            scope: me
        });

        searchButton.on({
            click: me.applyFilters,
            scope: me
        });

        clearFiltersButton.on({
            click: me.clearFilters,
            scope: me
        });
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

        removableProperties.on({
            add: me.onUpdateRemovablesStore,
            load: me.onUpdateRemovablesStore,
            update: me.onUpdateRemovablesStore,
            remove: me.onUpdateRemovablesStore,
            bulkremove: me.onUpdateRemovablesStore,
            scope: me
        });

        resultsStore.on({
            beforeload: me.onBeforeLoad,
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

    updateRemovableContainerVisibility: function () {
        var me = this,
            container = me.getRemovablePropertiesContainer(),
            itemCount = container.items.length,
            placeholder = me.getRemovablePropertiesPlaceholder();

        placeholder.setVisible(itemCount > 0);
        container.setVisible(itemCount > 0);
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

        Uni.util.Filters.loadHistoryState(me.filters, true);
        me.applyFilters();

        Ext.resumeLayouts(true);
    },

    initStickyCriteria: function () {
        var me = this,
            propertiesStore = Ext.getStore('Uni.store.search.Properties');

        propertiesStore.filter('sticky', true);

        me.removeStickyProperties();
        propertiesStore.each(function (property) {
            me.addStickyProperty(property);
        });

        propertiesStore.clearFilter();
    },

    initRemovableCriteria: function () {
        var me = this;

        me.removeRemovableProperties();
        me.updateRemovableContainerVisibility();
    },

    onUpdateRemovablesStore: function () {
        var me = this,
            emptyText = Uni.I18n.translate('search.overview.addCriteria.emptyText', 'UNI', 'Add criteria'),
            addCriteriaCombo = me.getAddCriteriaCombo(),
            criteriaStore = addCriteriaCombo.store;

        if (criteriaStore.count() === 0) {
            emptyText = Uni.I18n.translate('search.overview.addCriteria.emptyText.none', 'UNI', 'No criteria to add');
        }

        if (addCriteriaCombo.inputEl.dom.placeholder !== emptyText && addCriteriaCombo.emptyText !== emptyText) {
            addCriteriaCombo.inputEl.dom.placeholder = emptyText;
            addCriteriaCombo.emptyText = emptyText;
        }
    },

    onChangeSearchDomain: function (field, newValue, oldValue, options) {
        var me = options.scope,
            searchDomains = Ext.getStore('Uni.store.search.Domains'),
            searchDomain = searchDomains.findRecord('id', newValue),
            searchResults = Ext.getStore('Uni.store.search.Results'),
            searchProperties = Ext.getStore('Uni.store.search.Properties'),
            fields = Ext.getStore('Uni.store.search.Fields'),
            model = 'Uni.model.search.Result';

        if (searchDomain !== null && Ext.isDefined(searchDomain)) {
            me.updateSearchDomainHistoryState(searchDomain);

            searchProperties.getProxy().url = searchDomain.get('glossaryHref');
            searchProperties.load();

            fields.getProxy().url = searchDomain.get('describedByHref');
            fields.load({
                callback: function (records, operation, success) {
                    if (success) {
                        me.updateResultModelAndColumnsFromFields(records);

                        searchResults.removeAll();
                        searchResults.getProxy().url = searchDomain.get('selfHref');
                        me.lastRequest = searchResults.load();
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
            displayValue = field.get('displayValue');

        return {
            dataIndex: propertyName,
            header: displayValue,
            flex: 1
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

    updateSearchDomainHistoryState: function (searchDomain) {
        var params = {searchDomain: searchDomain.get('displayValue')},
            href = Uni.util.QueryString.buildHrefWithQueryString(params, false);

        if (location.href !== href) {
            Uni.util.History.suspendEventsForNextCall();
            location.href = href;
        }
    },

    getSearchDomainHistoryValue: function () {
        return Uni.util.QueryString.getQueryStringValues().searchDomain;
    },

    onSelectAddCriteria: function (field, records, options) {
        var me = options.scope,
            property = Ext.isArray(records) && !Ext.isEmpty(records) ? records[0] : null,
            addCriteriaCombo = me.getAddCriteriaCombo(),
            criteriaStore = field.store;

        if (property !== null) {
            me.addRemovableProperty(records[0]);
            criteriaStore.remove(records[0]);
        }

        addCriteriaCombo.setValue(undefined, false);
    },

    removeStickyProperties: function () {
        var me = this,
            property;

        Ext.suspendLayouts();

        me.filters.each(function (filter) {
            property = filter.property;

            if (property.get('sticky')) {
                me.removeStickyProperty(property);
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

            if (removable) {
                filter.widget.on('filterupdate', me.applyFilters, me);
            } else {
                filter.on('filterupdate', me.applyFilters, me);
            }

            me.updateRemovableContainerVisibility();
        }
    },

    removeProperty: function (property, container, removable) {
        var me = this,
            removables = me.getStore('Uni.store.search.Removables'),
            filterResult = undefined;

        me.filters.each(function (filter) {
            if (filter.property === property) {
                filterResult = filter;
                return false;
            }
        });

        if (Ext.isDefined(filterResult)) {
            if (removable) {
                filterResult.widget.un('filterupdate', me.applyFilters, me);
                filterResult.widget.resetValue();

                me.filters.remove(filterResult);
                container.remove(filterResult);

                try {
                    removables.add(property);
                } catch (ex) {
                    // Ignore the exceptions caused by not rendered components.
                }
            } else {
                filterResult.un('filterupdate', me.applyFilters, me);
                filterResult.resetValue();

                me.filters.remove(filterResult);
                container.remove(filterResult);
            }

            Uni.util.Filters.updateHistoryState(me.filters);
            me.updateRemovableContainerVisibility();
        }
    },

    addStickyProperty: function (property) {
        this.addProperty(property, this.getStickyPropertiesContainer());
    },

    removeStickyProperty: function (property) {
        this.removeProperty(property, this.getStickyPropertiesContainer());
    },

    removeRemovableProperties: function () {
        var me = this,
            criteriaStore = Ext.getStore('Uni.store.search.Removables'),
            property;

        Ext.suspendLayouts();

        me.filters.each(function (filter) {
            property = filter.property;

            if (!property.get('sticky')) {
                me.removeRemovableProperty(property);

                try {
                    criteriaStore.add(property);
                } catch (ex) {
                    // Ignore the exceptions caused by not rendered components.
                }
            }
        });

        Ext.resumeLayouts(true);
    },

    addRemovableProperty: function (property) {
        this.addProperty(property, this.getRemovablePropertiesContainer(), true);
    },

    removeRemovableProperty: function (property) {
        this.removeProperty(property, this.getRemovablePropertiesContainer(), true);
    },

    createWidgetForProperty: function (property, removable) {
        var me = this,
            type = property.get('type'),
            displayValue = property.get('displayValue'),
            widget = undefined;

        if (property.get('exhaustive')) {
            var store = Ext.create('Uni.store.search.PropertyValues', {
                proxy: {
                    type: 'ajax',
                    url: property.get('linkHref'),
                    reader: {
                        type: 'json',
                        root: 'values'
                    }
                }
            });

            widget = Ext.create('Uni.grid.filtertop.ComboBox', {
                emptyText: displayValue,
                store: store,
                valueField: 'id',
                displayField: 'displayValue',
                multiSelect: true
            });

            widget.on('afterrender', function () {
                store.load();
            }, me);
        } else {
            switch (type) {
                case 'String':
                    widget = Ext.create('Uni.grid.filtertop.Text', {
                        emptyText: displayValue
                    });
                    break;
                case 'BigDecimal':
                    widget = Ext.create('Uni.grid.filtertop.Number', {
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
                    me.removeRemovableProperty(property);
                }
            });
        }

        return widget;
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
    }
});