/**
 * @class Uni.controller.Search
 */
Ext.define('Uni.controller.Search', {
    extend: 'Ext.app.Controller',

    stores: [
        'Uni.store.search.Domains',
        'Uni.store.search.Properties',
        'Uni.store.search.Removables',
        'Uni.store.search.Results'
    ],

    requires: [
        'Uni.view.search.Overview',
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
        }
    ],

    filterObjectParam: 'filter',

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
    },

    initRemovableCriteria: function () {
        var me = this,
            removablesStore = Ext.getStore('Uni.store.search.Removables');

        me.removeRemovableProperties();

        // TODO Add removables that are in the URL filled in.

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
            searchProperties = Ext.getStore('Uni.store.search.Properties');

        if (searchDomain !== null && Ext.isDefined(searchDomain)) {
            me.updateSearchDomainHistoryState(searchDomain);

            searchResults.getProxy().url = searchDomain.get('selfHref');
            searchResults.load();

            searchProperties.getProxy().url = searchDomain.get('glossaryHref');
            searchProperties.load();
        }
    },

    updateSearchDomainHistoryState: function (searchDomain) {
        var params = {
                searchDomain: searchDomain.get('displayValue')
            },
            href = Uni.util.QueryString.buildHrefWithQueryString(params, false);

        if (location.href !== href) {
            debugger;
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

    addProperty: function (property, container) {
        var me = this,
            filter = me.createWidgetForProperty(property);

        if (Ext.isDefined(filter)) {
            me.filters.add(filter);
            container.add(filter);

            filter.on('filterupdate', me.applyFilters, me);
        }
    },

    removeProperty: function (property, container) {
        var me = this,
            filterResult = undefined;

        me.filters.each(function (filter) {
            if (filter.property === property) {
                filterResult = filter;
                return false;
            }
        });

        if (Ext.isDefined(filterResult)) {
            me.filters.remove(filterResult);
            container.remove(filterResult);

            filterResult.un('filterupdate', me.applyFilters, me)
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
                criteriaStore.add(property);
            }
        });

        Ext.resumeLayouts(true);
    },

    addRemovableProperty: function (property) {
        this.addProperty(property, this.getRemovablePropertiesContainer());
    },

    removeRemovableProperty: function (property) {
        this.removeProperty(property, this.getRemovablePropertiesContainer());
    },

    createWidgetForProperty: function (property) {
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
                default:
                    // <debug>
                    console.log('Unknown search property type: ' + type);
                    // </debug>

                    break;
            }
        }

        Ext.apply(widget, {
            dataIndex: property.get('name'),
            property: property
        });

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
        Ext.getStore('Uni.store.search.Results').load();
    }
});