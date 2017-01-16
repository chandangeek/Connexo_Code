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
        'Uni.model.search.Value',
        'Uni.view.search.field.internal.CriteriaButton',
        'Uni.view.search.field.internal.Adapter',
        'Uni.grid.column.Date'
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
    isStateLoad: false,

    /**
     * search domain
     */
    searchDomain: null,

    /**
     * Criteria state
     */
    criteria: Ext.create('Ext.util.MixedCollection', false, function (o) {
        return o ? o.getId() : null;
    }),

    /**
     * Filter state
     */
    filters: Ext.create('Ext.util.MixedCollection', false, function (f) {
        return f.id
    }),

    criteriaMap: {
        'Boolean:com.elster.jupiter.properties.BooleanFactory':              'uni-search-criteria-boolean',
        'Instant:com.elster.jupiter.properties.InstantFactory':              'uni-search-criteria-datetime',
        'TimeDuration:com.elster.jupiter.properties.StringReferenceFactory': 'uni-search-criteria-timeduration',
        'TimeDuration:com.energyict.mdc.dynamic.TimeDurationValueFactory':   'uni-search-criteria-timeduration',
        'BigDecimal:com.elster.jupiter.properties.BigDecimalFactory':        'uni-search-criteria-numeric',
        'Long:com.elster.jupiter.properties.LongFactory':                    'uni-search-criteria-numeric',
        'Date:com.energyict.mdc.dynamic.DateFactory':                        'uni-search-criteria-date',
        'Date:com.energyict.mdc.dynamic.DateAndTimeFactory':                 'uni-search-criteria-clock',
        'TimeOfDay:com.energyict.mdc.dynamic.TimeOfDayFactory':              'uni-search-criteria-timeofday',
        'ObisCode:com.energyict.mdc.dynamic.ObisCodeValueFactory':           'uni-search-criteria-obis'
    },

    fieldMap: {
        'Boolean': 'boolean',
        'Instant': 'date',
        'Long': 'int',
        'String': 'string'
    },

    columnMap: {
        'Long': 'numbercolumn',
        'Date': 'datecolumn',
        'Instant': 'uni-date-column',
        'Boolean': 'uni-grid-column-search-boolean',
        'DeviceType': 'uni-grid-column-search-devicetype',
        'DeviceConfiguration': 'uni-grid-column-search-deviceconfiguration',
        'Quantity': 'uni-grid-column-search-quantity'
    },

    /*defaultColumns: {
        'com.energyict.mdc.device.data.Device': ['id', 'mRID', 'serialNumber', 'deviceTypeName', 'deviceConfigurationName', 'state.name', 'location'],
        'com.elster.jupiter.metering.UsagePoint': ['mRID', 'displayServiceCategory', 'displayMetrologyConfiguration']
     },*/

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
            domain = domains.getById(domain);
        }

        if (domain !== null
            && Ext.isDefined(domain)
            && Ext.getClassName(domain) == "Uni.model.search.Domain"
        ) {
            me.fireEvent('setDomain', domain);
            me.searchDomain = domain;

            me.filters.removeAll();
            me.criteria.removeAll();

            searchProperties.removeAll();
            searchFields.removeAll();
            searchResults.removeAll(true);

            searchProperties.getProxy().url = domain.get('glossaryHref');
            searchFields.getProxy().url     = domain.get('describedByHref');
            searchResults.getProxy().url    = domain.get('selfHref');

            if (!me.isStateLoad) {
                searchFields.clearFilter(true);
                searchResults.clearFilter(true);
                searchProperties.clearFilter(true);
            }

            searchProperties.load(function(){
                me.fireEvent('reset', me.filters);
                me.init();
                searchFields.load(function(){
                    callback ? callback() : null;
                });
            });
        }
    },

    onSearchFieldsLoad: function(store, records, success) {
        var me = this,
            resultsStore = this.getSearchResultsStore();

        Ext.suspendLayouts();
        resultsStore.removeAll(true);
        resultsStore.model.setFields(records.map(function (field) {
            return me.createFieldDefinitionFromModel(field)
        }));

        if (!me.isStateLoad) {
            this.applyFilters();
        }
        Ext.resumeLayouts(true);
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

        me.initCriteria();
        me.saveState();

        Ext.resumeLayouts(true);
    },

    onSearchPropertiesLoad: function (store) {
        var me = this;

        me.criteria.each(function(prop){
            if (!store.getById(prop.getId())) {
                me.removeProperty(prop);
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

    /**
     * Adds criteria to criteria state
     * Fires event
     *
     * @param property Critera
     */
    addProperty: function (property) {
        var me = this;

        me.criteria.add(property);
        me.fireEvent('add', me.criteria, property);

        return property;
    },

    /**
     * Removes criteria from criteria state
     * Fires event
     *
     * @param property Critera
     */
    removeProperty: function (property) {
        var me = this,
            removed = me.criteria.remove(property),
            filter =  me.filters.removeAtKey(property.getId());

        if (removed) {
            me.fireEvent('remove', me.criteria, property);
        }

        if (filter) {
            filter.value = null;
            me.fireEvent('change', me.filters, filter);

            if (property.get('affectsAvailableDomainProperties')
                && !me.isStateLoad) {
                me.storeReload(me.getSearchPropertiesStore());
            }
        }
    },

    applyFilters: function () {
        var me = this,
            searchResults = me.getSearchResultsStore(),
            filters = me.getFilters();

        searchResults.clearFilter(true);
        if (filters && filters.length) {
            if(searchResults.isLoading()){
                Ext.Ajax.suspendEvent('requestexception');
                Ext.Ajax.abort(searchResults.lastRequest);
                Ext.Ajax.resumeEvent('requestexception');
            }
            searchResults.addFilter(me.getFilters(), false);
            me.fireEvent('applyFilters', me, filters);
            searchResults.loadPage(1);
        } else {
            searchResults.removeAll();
            searchResults.fireEvent('load', searchResults, [], true);
            me.fireEvent('applyFilters', me, filters);
        }
    },

    count: function(){
        var me = this;
        me.fireEvent('loadingcount');
        Ext.Ajax.request({
            url: this.getSearchResultsStore().getProxy().url + '/count',
            timeout: 120000,
            method: 'GET',
            params: {
                filter: JSON.stringify(this.getFilters())
            },
            success: function (response) {
                me.fireEvent('count', JSON.parse(response.responseText));
            }
        });
    },

    clearFilters: function () {
        var me = this;

        me.getSearchResultsStore().removeAll();
        me.setDomain(me.searchDomain, function() {
            me.applyFilters();
        })
    },

    getFilters: function () {
        return _.filter(this.filters.getRange(), function(f){
            return !!f.value
                && Ext.isArray(f.value)
                && !Ext.isEmpty(_.filter(f.value, function(v) { return !Ext.isEmpty(v.criteria);}))
        });
    },

    getState: function() {
        return {
            domain: this.getDomain() ? this.getDomain().getId() : null,
            filters: this.getFilters().map(function(item){return _.pick(item, 'property', 'value')})
        }
    },

    applyState: function (state, callback) {
        var me = this,
            propertiesStore = me.getSearchPropertiesStore(),
            resultsStore = me.getSearchResultsStore(),
            filters = _.map(state.filters, function (f) {
                return new Ext.util.Filter({
                    property: f.property,
                    value: f.value,
                    id: f.property
                });
            });

        me.isStateLoad = true;
        resultsStore.clearFilter(true);
        propertiesStore.clearFilter(true);
        resultsStore.addFilter(filters, false);
        propertiesStore.addFilter(filters, false);

        me.setDomain(state.domain, function() {
            if (filters && filters.length) {
                me.setFilters(filters);
                resultsStore.load();
            } else {
                resultsStore.removeAll();
                resultsStore.fireEvent('load', resultsStore, [], true);
            }

            me.isStateLoad = false;
            callback ? callback() : null;
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
            disabled: propertyName === 'name',
            xtype: type
        };
    },

    createFieldDefinitionFromModel: function (field) {
        var propertyName = field.get('propertyName'),
            type = this.fieldMap[field.get('type')];

        if (!type) {
            type = 'auto';
        }

        var config = {
            name: propertyName,
            type: type
        };

        if (config.type == 'date') {
            Ext.apply(config, {
                dateFormat: 'time'
            })
        }

        return config;
    },

    checkConstraints: function (property) {
        var me = this;

        return !!property.get('constraints').filter(function (c) {
            return !_.find(me.getFilters(), function (f) {
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
                dataIndex: property.getId(),
                itemId: 'criteria-' + property.getId(),
                property: property,
                listeners: {
                    change: {
                        fn: function(widget, value) {
                            me.setFilter(new Ext.util.Filter({
                                property: widget.dataIndex,
                                value: value && widget.isValid() ? value.map(function(v){return v.getData()}) : null,
                                id: widget.dataIndex
                            }));
                        },
                        scope: me
                    }
                }
            };

        if (property.get('exhaustive')) {
            Ext.apply(config, {
                xtype: 'uni-search-criteria-selection',
                emptyText: displayValue,
                store: property.values(),
                valueField: 'id',
                displayField: 'displayValue',
                multiSelect: property.get('selectionMode') === 'multiple'
            });
        }

        if (property.get('type') === 'Quantity') {
            Ext.apply(config, {
                xtype: 'uni-search-criteria-quantity'
            });
        }

        if (property.get('name') === 'location') {
            Ext.apply(config, {
                xtype: 'uni-search-criteria-location'
            });
        }

        if (Ext.isEmpty(config.xtype)) {
            Ext.apply(config, {
                xtype: 'uni-search-criteria-simple'
            });
        }

        return config;
    },

    createCriteriaButton: function (property) {
        var me = this,
            displayValue = property.get('displayValue'),
            config = {
                xtype: 'uni-search-internal-button',
                text: displayValue,
                dataIndex: property.getId(),
                itemId: 'criteria-' + property.getId(),
                property: property,
                service: me
            };

        if (property.get('constraints') && property.get('constraints').length) {
            property.beginEdit();
            property.values().addFilter(me.getFilters(), false);
            if (me.checkConstraints(property)) {
                property.set('disabled', true);
            }
            property.endEdit(true);
        }

        if (!property.get('sticky')) {
            config = {
                xtype: 'uni-search-internal-adapter',
                widget: config,
                removeHandler: function () {
                    me.removeProperty(property);
                }
            };
        }

        return config;
    },

    getDependentProperties: function (property) {
        var me = this;

        return me.criteria.filterBy(function (p) {
            return !!(p.get('constraints')
            && p.get('constraints').length
            && p.get('constraints').indexOf(property.getId()) >= 0);
        });
    },

    setFilters: function(filters) {
        var me = this;

        Ext.suspendLayouts();
        me.filters.removeAll();
        me.filters.add(filters);
        Ext.resumeLayouts(true);

        filters.map(function(filter) {
            me.onFilterChange(filter);
        });

        me.saveState();

    },

    setFilter: function (filter) {
        var me = this;

        Ext.suspendLayouts();
        me.filters.add(filter);
        me.onFilterChange(filter);
        me.saveState();
        Ext.resumeLayouts(true);
    },

    onFilterChange: function(filter) {
        var me = this,
            propertiesStore = me.getSearchPropertiesStore(),
            property = me.criteria.get(filter.id) || me.addProperty(propertiesStore.getById(filter.id)),
            deps = me.getDependentProperties(property);

        if (property.get('affectsAvailableDomainProperties')
            && !me.isStateLoad) {
            me.storeReload(propertiesStore);
        }

        if (deps.length) {
            deps.each(function(criteria) {
                criteria.beginEdit();
                if (filter && !Ext.isEmpty(filter.value)) {
                    criteria.set('disabled', false);
                } else {
                    if (!criteria.get('sticky')) {
                        me.removeProperty(criteria);
                    }
                    criteria.set('disabled', true);
                }
                criteria.endEdit(true);

                criteria.isCached = false;
                criteria.values().clearFilter(true);
                criteria.values().addFilter(me.getFilters(), false);

                var f = me.filters.getByKey(criteria.getId());
                if (f) {
                    criteria.refresh(function () {
                        f.value = _.map(f.value, function(v) {
                            return Ext.apply(v, {
                                criteria: _.intersection(v.criteria, _.map(criteria.values().data.keys, function(v){return v.toString()}))
                            })
                        });
                        me.fireEvent('change', me.filters, f);
                        me.fireEvent('criteriaChange', me.criteria, criteria);
                    });
                } else {
                    if(criteria.get('exhaustive')) {
                        criteria.refresh(function () {
                            me.fireEvent('criteriaChange', me.criteria, criteria);
                        });
                    }
                }
                me.fireEvent('criteriaChange', me.criteria, criteria);
            });
        }

        me.fireEvent('change', me.filters, filter);
    },

    /**
     * Utility function
     *
     * @param store
     * @param callback
     */
    storeReload: function (store, callback) {
        var me = this;

        if (store.isLoading() && store.lastRequest) {
            Ext.Ajax.suspendEvent('requestexception');
            Ext.Ajax.abort(store.lastRequest);
            Ext.Ajax.resumeEvent('requestexception');
        }
        store.clearFilter(true);
        store.addFilter(me.getFilters(), false);
        store.load({
            callback: callback
        });
        store.lastRequest = Ext.Ajax.getLatest();
    }
});