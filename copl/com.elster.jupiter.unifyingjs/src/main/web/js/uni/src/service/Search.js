Ext.define('Uni.service.Search', {

    mixins: {
        observable: 'Ext.util.Observable',
        stateful: 'Ext.state.Stateful'
    },

    requires: [
        'Uni.store.search.Domains',
        'Uni.store.search.Results',
        'Uni.store.search.Properties',
        'Uni.store.search.Fields',
        'Uni.store.search.PropertyValues',
        'Ext.state.Manager',
        'Ext.state.LocalStorageProvider',
        'Uni.model.search.Value'
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
            'reset',
            'searchResultsBeforeLoad'
        );
    },

    stateful: true,
    stateId: 'search',

    /**
     * search domain
     */
    searchDomain: null,

    filters: Ext.create('Ext.util.MixedCollection', false, function (o) {
        return o ? o.dataIndex : null;
    }),

    criteriaMap: {
        'Boolean:com.elster.jupiter.properties.BooleanFactory':              'uni-search-criteria-boolean',
        'Instant:com.elster.jupiter.properties.InstantFactory':              'uni-search-criteria-datetime',
        'TimeDuration:com.elster.jupiter.properties.StringReferenceFactory': 'uni-search-criteria-timeduration',
        'BigDecimal:com.elster.jupiter.properties.BigDecimalFactory':        'uni-search-criteria-numeric',
        'Date:com.energyict.mdc.dynamic.DateFactory':                        'uni-search-criteria-date',
        'Date:com.energyict.mdc.dynamic.DateAndTimeFactory':                 'uni-search-criteria-clock',
        'TimeOfDay:com.energyict.mdc.dynamic.TimeOfDayFactory':              'uni-search-criteria-timeofday'
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
        'com.energyict.mdc.device.data.Device': ['id', 'mRID', 'serialNumber', 'deviceTypeName', 'deviceConfigurationName', 'state.name'],
        'com.elster.jupiter.metering.UsagePoint' : ['mRID', 'serviceCategory', 'connectionState', 'openIssues']
    },

    getDomain: function() {
        return this.searchDomain;
    },

    initStoreListeners: function() {
        var me = this;

        me.unbind();
        me.storeListeners.push(me.getSearchResultsStore().on({
            load: me.onSearchResultsLoad,
            beforeload: me.onSearchResultsBeforeLoad,
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
     * @param callback
     */
    setDomain: function(domain, callback) {
        var me = this,
            searchResults = me.getSearchResultsStore(),
            searchProperties = me.getSearchPropertiesStore(),
            searchFields = me.getSearchFieldsStore(),
            domains = me.getSearchDomainsStore();

        //domains.load(function())
        if (Ext.isString(domain)) {
            domain = domains.findRecord('id', domain, 0, true, true);
        }

        if (domain !== null
            && Ext.isDefined(domain)
            && Ext.getClassName(domain) == "Uni.model.search.Domain"
        ) {
            //todo: how to set domain from state && manually at same time?
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
                callback ? callback() : null;
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

    onSearchResultsBeforeLoad: function () {
        var me = this;
        me.fireEvent('searchResultsBeforeLoad');
    },

    init: function() {
        var me = this;

        Ext.getStore('Uni.property.store.TimeUnits').load();
        me.initStoreListeners();
        Ext.suspendLayouts();

        me.filters.removeAll();
        me.fireEvent('reset', me.filters);

        me.initCriteria();
        me.saveState();

        Ext.resumeLayouts(true);
    },

    onSearchPropertiesLoad: function (store) {
        var me = this;

        me.filters.each(function(filter){
            if (!store.getById(filter.property.getId())) {
                me.removeProperty(filter.property);
            }
        });
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

        filter = me.filters.get(property.getId());

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
        searchResults.addFilter(me.getFilters(), false);
        searchResults.load();
    },

    clearFilters: function () {
        this.init();
        this.applyFilters();
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

    getState: function() {
        return {
            domain: this.getDomain() ? this.getDomain().getId() : null,
            filters: this.getFilters().map(function(item){return _.pick(item, 'property', 'value')})
        }
    },

    applyState: function (state) {
        var me = this,
            propertiesStore = me.getSearchPropertiesStore();

        propertiesStore.addFilter(state.filters, false);

        me.setDomain(state.domain, function() {
            if (state.filters && state.filters.length) {
                Ext.suspendLayouts();

                state.filters.map(function(item) {
                    var property = propertiesStore.getById(item.property);
                    if (property && property.get('visibility') === 'removable') {
                        me.addProperty(property);
                    }
                    var filter = me.filters.getByKey(item.property);
                    if (filter && item.value) {
                        var value = Ext.isArray(item.value) ? item.value : [item.value];
                        filter.populateValue(value.map(function(rawValue) { return Ext.create('Uni.model.search.Value', rawValue)}));
                    }
                });

                Ext.resumeLayouts(true);
            }
        });
    },

    createColumnDefinitionFromModel: function (field) {
        var propertyName = field.get('propertyName'),
            type = this.columnMap[field.get('type')],
            displayValue = field.get('displayValue'),
            defaultColumns = this.defaultColumns[this.getDomain().get('id')];

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
            type = property.get('type') + ':' + property.get('factoryName'),
            displayValue = property.get('displayValue'),
            config = {
                xtype: me.criteriaMap[type],
                text: displayValue,
                dataIndex: property.get('name'),
                itemId: 'criteria-' + property.get('name'),
                property: property,
                listeners: {
                    'change': {
                        fn: me.onCriteriaChange,
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

    onCriteriaChange: function (widget, value) {
        var me = this, store;

        if (widget.property.get('affectsAvailableDomainProperties')) {
            store = me.getSearchPropertiesStore();
            store.clearFilter(true);
            store.addFilter(me.getFilters(), false);
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
                        store.addFilter(me.getFilters(), false);
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
                        item.getStore().clearFilter(true);
                    }
                    if (!item.property.get('sticky')) {
                        me.removeProperty(item.property);
                    }
                }
            });
        }

        me.saveState();
        me.fireEvent('change', widget, value);
    }
});