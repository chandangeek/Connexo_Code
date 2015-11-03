Ext.define('Uni.service.Search', {
    mixins: {
        observable: 'Ext.util.Observable'
    },

    requires: [
        'Uni.store.search.Domains',
        'Uni.store.search.Results',
        'Uni.store.search.Properties',
        'Uni.store.search.Fields',
        'Uni.store.search.PropertyValues'
    ],

    config: {
        router: null,
        searchDomainsStore: 'Uni.store.search.Domains', //Ext.getStore('Uni.store.search.Domains'),
        searchResultsStore: 'Uni.store.search.Results',
        searchPropertiesStore: 'Uni.store.search.Properties',
        searchFieldsStore: 'Uni.store.search.Fields'
    },

    storeListeners: [],

    constructor: function (config) {
        var me = this;

        me.setSearchDomainsStore(Ext.getStore(me.getSearchDomainsStore() || 'ext-empty-store'));
        me.setSearchResultsStore(Ext.getStore(me.getSearchResultsStore() || 'ext-empty-store'));
        me.setSearchPropertiesStore(Ext.getStore(me.getSearchPropertiesStore() || 'ext-empty-store'));
        me.setSearchFieldsStore(Ext.getStore(me.getSearchFieldsStore() || 'ext-empty-store'));

        // The Observable constructor copies all of the properties of `config` on
        // to `this` using Ext.apply. Further, the `listeners` property is
        // processed to add listeners.
        //
        me.mixins.observable.constructor.call(this, config);

        me.addEvents(
            'add',
            'remove',
            'change',
            'reset'
        );
    },

    /**
     * search domain
     */
    searchDomain: null,

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

    getDomain: function() {
        return this.searchDomain;
    },

    updateState: function() {
        //if (router.queryParams.searchDomain !== searchDomain.get('displayValue')) {
        //    Uni.util.History.suspendEventsForNextCall();
        //    Uni.util.History.setParsePath(false); //todo: this is probably a bug un unifying, so this line shouldn't be here
        //    router.getRoute().forward(null, {searchDomain: searchDomain.get('displayValue')});
        //}
    },

    initStoreListeners: function() {
        var me = this;

        me.unbind();
        me.storeListeners.push(me.getSearchResultsStore().on({
            load: me.onSearchResultsLoad,
            scope: me,
            destroyable: true
        }));

        me.storeListeners.push(me.getSearchPropertiesStore().on({
            load: me.onSearchPropertiesLoad,
            scope: me,
            destroyable: true
        }));

        me.storeListeners.push(me.getSearchFieldsStore().on({
            load: me.onSearchFieldsLoad,
            scope: me,
            destroyable: true
        }));
    },

    unbind: function() {
        this.storeListeners.map(function (listener) {
            listener.destroy();
        });
    },

    /**
     * @param domain string or Domain model
     */
    setDomain: function(domain) {
        var me = this,
            searchResults = me.getSearchResultsStore(),
            searchProperties = me.getSearchPropertiesStore(),
            searchFields = me.getSearchFieldsStore();

        if (Ext.isString(domain)) {
            domain = me.getSearchDomainsStore().findRecord('id', domain, 0, true, true);
        }

        if (domain !== null && Ext.isDefined(domain) && Ext.getClassName(domain) == "Uni.model.search.Domain") {
            me.searchDomain = domain;

            searchProperties.removeAll();
            searchFields.removeAll();
            searchResults.removeAll(true);

            searchProperties.getProxy().url = domain.get('glossaryHref');
            searchFields.getProxy().url     = domain.get('describedByHref');
            searchResults.getProxy().url    = domain.get('selfHref');

            searchProperties.load(function(){
                me.init();
                searchFields.load();
            });
        }
    },

    onSearchFieldsLoad: function(store, records, success) {
        if (success) {
            this.getSearchResultsStore().removeAll(true);
            this.applyFilters();
        }
    },

    onSearchResultsLoad: function(store, records, success) {
        if (!success) {
            store.removeAll();
        }
    },

    init: function() {
        var me = this;

        me.initStoreListeners();
        Ext.suspendLayouts();

        me.fireEvent('reset', me.filters);
        me.filters.removeAll();

        me.initCriteria();
        me.restoreState();

        Ext.resumeLayouts(true);
    },

    onSearchPropertiesLoad: function () {
        //todo: post criteria update functions
    },

    initCriteria: function () {
        var me = this,
            propertiesStore = me.getSearchPropertiesStore();

        propertiesStore.each(function (property) {
            if (property.get('sticky')) {
                me.addProperty(property);
            }
        });
    },

    addProperty: function (property) {
        var me = this,
            filter = me.createWidgetForProperty(property);

        if (Ext.isDefined(filter)) {
            me.filters.add(property.get('sticky') ? filter : filter.widget);
            me.fireEvent('add', me.filters, filter, property);
        }
    },

    removeProperty: function (property) {
        var me = this,
            filter;

        filter = me.filters.findBy(function (filter) {
            return filter.property === property;
        });

        if (filter) {
            filter.reset();
            me.filters.remove(filter);
            me.fireEvent('remove', me.filters, filter, property);
        }
    },

    applyFilters: function () {
        var me = this,
            searchResults = me.getSearchResultsStore();

        searchResults.clearFilter(true);
        searchResults.filter(me.getFilters(), true);
        searchResults.load();
    },

    clearFilters: function () {
        var me = this;

        me.onSearchPropertiesLoad();
    },

    getFilters: function() {
        var me = this,
            filters = [];

        me.filters.each(function (item) {
            if (!Ext.isEmpty(item.getValue())) {
                filters.push(item.getFilter());
            }
        });

        return filters;
    },

    restoreState: function () {
        var me = this,
            router = me.getRouter(),
            propertiesStore = me.getSearchPropertiesStore();

        if (router.queryParams[me.filterObjectParam]) {
            Ext.suspendLayouts();
            var filter = JSON.parse(router.queryParams[me.filterObjectParam]);
            filter.map(function(item) {
                var property = propertiesStore.findRecord('name', item.property);
                if (property && property.get('visibility') === 'removable') {
                    me.addProperty(property);
                }
                var filter = me.filters.getByKey(item.property);
                if (filter) {
                    filter.populateValue(item.value);
                }
            });

            Ext.resumeLayouts(true);
        }
    },

    createColumnDefinitionFromModel: function (field) {
        var propertyName = field.get('propertyName'),
            type = this.columnMap[field.get('type')],
            displayValue = field.get('displayValue'),
            defaultColumns = this.defaultColumns[this.searchDomain.get('id')];

        if (!type) {
            type = 'gridcolumn';
        }

        return {
            isDefault: defaultColumns && defaultColumns.indexOf(field.get('propertyName')) >= 0,
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

    checkConstraints: function (property) {
        var me = this;

        return !!property.get('constraints').filter(function (c) {
            return !me.getFilters().find(function (f) {
                return (f.id === c) && f.value
            })
        }).length
    },

    createWidgetForProperty: function (property) {
        var me = this,
            type = property.get('type'),
            displayValue = property.get('displayValue'),
            config = {
                xtype: me.criteriaMap[type],
                text: displayValue,
                dataIndex: property.get('name'),
                itemId: 'criteria-' + property.get('name'),
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

            if (property.get('constraints')) {
                var filters = _.filter(me.getFilters(), function (f) {
                    return property.get('constraints').indexOf(f.id >= 0);
                });

                store.addFilter(filters, false);
            }
            store.load();

            Ext.apply(config, {
                xtype: 'uni-search-criteria-selection',
                emptyText: displayValue,
                store: store,
                valueField: 'id',
                displayField: 'displayValue',
                multiSelect: property.get('selectionMode') === 'multiple'
            });
        }

        if (property.get('constraints') && property.get('constraints').length && me.checkConstraints(property)) {
            Ext.apply(config, {
                disabled: true
            });
        }

        if (Ext.isEmpty(config.xtype)){
            Ext.apply(config, {
                xtype: 'uni-search-criteria-simple'
            });
        }

        widget = Ext.widget(config);

        if (!property.get('sticky')) {
            widget = Ext.create('Uni.view.search.field.internal.Adapter', {
                widget: widget,
                removeHandler: function () {
                    me.removeProperty(property);
                }
            });
        }

        return widget;
    },

    updateConstraints: function (widget, value) {
        var me = this, store;

        if (widget.property.get('affectsAvailableDomainProperties')) {
            store = me.getSearchPropertiesStore();
            store.clearFilter(true);
            store.addFilter(widget.getFilter(), false);
            store.load();
        }

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

        me.fireEvent('change', widget, value);
    }
});