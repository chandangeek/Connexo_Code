/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        searchDomainsStore: 'Uni.store.search.Domains',
        searchResultsStore: 'Uni.store.search.Results',
        searchPropertiesStore: 'Uni.store.search.Properties',
        searchFieldsStore: 'Uni.store.search.Fields'
    },

    storeListeners: [],
    changedFiltersNotYetApplied: false,
    previouslyAppliedFiltersAsString: undefined,
    previouslyAppliedState: undefined,
    bulkAction: undefined,

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
        'Boolean:com.elster.jupiter.properties.BooleanFactory': 'uni-search-criteria-boolean',
        'Instant:com.elster.jupiter.properties.InstantFactory': 'uni-search-criteria-datetime',
        'TimeDuration:com.elster.jupiter.properties.StringReferenceFactory': 'uni-search-criteria-timeduration',
        'TimeDuration:com.energyict.mdc.dynamic.TimeDurationValueFactory': 'uni-search-criteria-timeduration',
        'BigDecimal:com.elster.jupiter.properties.BigDecimalFactory': 'uni-search-criteria-numeric',
        'Long:com.elster.jupiter.properties.LongFactory': 'uni-search-criteria-numeric',
        'Date:com.energyict.mdc.dynamic.DateFactory': 'uni-search-criteria-date',
        'Date:com.energyict.mdc.dynamic.DateAndTimeFactory': 'uni-search-criteria-clock',
        'TimeOfDay:com.energyict.mdc.dynamic.TimeOfDayFactory': 'uni-search-criteria-timeofday',
        'ObisCode:com.energyict.mdc.dynamic.ObisCodeValueFactory': 'uni-search-criteria-obis',
        'Expiration:com.elster.jupiter.properties.ExpirationFactory': 'uni-search-criteria-expiration'
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

    getDomain: function () {
        return this.searchDomain;
    },

    initStoreListeners: function () {
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

    unbind: function () {
        this.storeListeners.map(function (listener) {
            listener.destroy();
        });
    },

    /**
     * @param domain string or Domain model
     * @param callback
     */
    setDomain: function (domain, callback) {
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
            searchFields.getProxy().url = domain.get('describedByHref');
            searchResults.getProxy().url = domain.get('selfHref');

            if (!me.isStateLoad) {
                searchFields.clearFilter(true);
                searchResults.clearFilter(true);
                searchProperties.clearFilter(true);
            }

            searchProperties.load(function () {
                me.fireEvent('reset', me.filters);
                me.init();
                searchFields.load(function () {
                    callback ? callback() : null;
                });
            });
        }
    },

    onSearchFieldsLoad: function (store, records, success) {
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

    onSearchResultsLoad: function (store, records, success) {
        if (!success) {
            store.removeAll();
        }
    },

    onSearchResultsBeforeLoad: function () {
        var me = this;
        me.fireEvent('searchResultsBeforeLoad');
    },

    init: function () {
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

        me.criteria.each(function (prop) {
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
            filter = me.filters.removeAtKey(property.getId()),
            dependentProperties;

        if (removed) {
            dependentProperties = me.getDependentProperties(property);
            for (var i = 0; i < dependentProperties.length; i++) {
                me.removeProperty(dependentProperties.get(i));
            }
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
            filters = me.getFilters(),
            router = this.router;

        me.previouslyAppliedState = me.getState();
        me.previouslyAppliedFiltersAsString = JSON.stringify(filters);
        me.changedFiltersNotYetApplied = false;
        me.bulkAction = false;
        searchResults.clearFilter(true);
        if (filters && filters.length) {
            if (router && router.currentRoute == 'search') {
                Uni.util.History.setParsePath(false);
                router.getRoute('search').forward(null, Ext.apply(router.queryParams, {restore: true}));
            }

            if (searchResults.isLoading()) {
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

    count: function () {
        var me = this,
            performTheCount = function () {
                me.fireEvent('loadingcount');
                Ext.Ajax.request({
                    url: me.getSearchResultsStore().getProxy().url + '/count',
                    timeout: 120000,
                    method: 'POST',
                    params: {
                        filter: me.previouslyAppliedFiltersAsString
                    },
                    success: function (response) {
                        me.fireEvent('count', JSON.parse(response.responseText));
                    }
                });
            };

        if (me.changedFiltersNotYetApplied) {
            var confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: Uni.I18n.translate('general.apply', 'UNI', 'Apply'),
                secondConfirmText: Uni.I18n.translate('general.dontApply', 'UNI', "Don't apply"),
                green: true,
                confirmation: function (button) {
                    confirmationWindow.close();
                    if (button.action === 'confirm') { // (Re)apply the criteria first
                        me.getSearchResultsStore().on('load', function () {
                            performTheCount();
                        }, me, {single: true});
                        me.applyFilters();
                    } else if (button.action === 'confirm2') { // Don't (re)apply the cirteria
                        me.rollbackCriteriaChanges(performTheCount());
                    }
                }
            });
            confirmationWindow.show({
                title: Uni.I18n.translate('general.performCount', 'UNI', 'Perform count?'),
                msg: Uni.I18n.translate('general.unconfirmedSearchCriteria', 'UNI',
                    "Some search criteria haven't been applied. Do you want to apply them?")
            });
        } else {
            performTheCount();
        }
    },

    clearFilters: function () {
        var me = this;
        me.getSearchResultsStore().removeAll();
        me.setDomain(me.searchDomain, function () {
            me.applyFilters();
            if(me.loadCombo && me.saveSearchButton){
                me.loadCombo.clearValue();
                me.saveSearchButton.disable();
            }
        })
    },

    getFilters: function () {
        return _.filter(this.filters.getRange(), function (f) {
            return !!f.value
                && Ext.isArray(f.value)
                && !Ext.isEmpty(
                    _.filter(f.value, function(v) {
                        return !Ext.isEmpty(v.criteria)
                            || v.operator === 'ISDEFINED'
                            || v.operator === 'ISNOTDEFINED';
                    })
                )
        });
    },

    getState: function () {
        return {
            domain: this.getDomain() ? this.getDomain().getId() : null,
            filters: this.getFilters().map(function (item) {
                return _.pick(item, 'property', 'value')
            })
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

        me.setDomain(state.domain, function () {
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
            menuDisabled: true,
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
                        fn: function (widget, value) {
                            me.setFilter(new Ext.util.Filter({
                                property: widget.dataIndex,
                                value: value && widget.isValid() ? value.map(function (v) {
                                    return v.getData()
                                }) : null,
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

        if (property.get('name') === 'device.topology.master') {
            Ext.apply(config, {
                xtype: 'uni-search-criteria-has-string'
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

    setFilters: function (filters) {
        var me = this;

        Ext.suspendLayouts();
        me.filters.removeAll();
        me.filters.add(filters);
        Ext.resumeLayouts(true);

        filters.map(function (filter) {
            me.onFilterChange(filter);
        });

        me.saveState();
        me.changedFiltersNotYetApplied = me.bulkAction == true ? false : true;
    },

    setFilter: function (filter) {
        var me = this;

        Ext.suspendLayouts();
        me.filters.add(filter);
        me.onFilterChange(filter);
        me.saveState();
        Ext.resumeLayouts(true);
    },

    onFilterChange: function (filter) {
        var me = this,
            propertiesStore = me.getSearchPropertiesStore(),
            property = me.criteria.get(filter.id) || me.addProperty(propertiesStore.getById(filter.id)),
            deps = me.getDependentProperties(property);

        if (property.get('affectsAvailableDomainProperties') && !me.isStateLoad) {
            me.storeReload(propertiesStore);
        }

        if (deps.length) {
            deps.each(function (criteria) {
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
                        f.value = _.map(f.value, function (v) {
                            return Ext.apply(v, {
                                criteria: _.intersection(v.criteria, _.map(criteria.values().data.keys, function (v) {
                                    return v.toString()
                                }))
                            })
                        });
                        me.fireEvent('change', me.filters, f);
                        me.fireEvent('criteriaChange', me.criteria, criteria);
                    });
                } else if (criteria.get('exhaustive') && !Ext.isEmpty(filter.value)) {
                    criteria.refresh(function () {
                        me.fireEvent('criteriaChange', me.criteria, criteria);
                    });
                } else {
                    me.fireEvent('criteriaChange', me.criteria, criteria);
                }
            });

        }
        me.changedFiltersNotYetApplied = me.bulkAction;
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
    },

    rollbackCriteriaChanges: function (callback) {
        var me = this;
        if (!Ext.isEmpty(me.previouslyAppliedState)) {
            this.applyState(me.previouslyAppliedState, callback);
        }
    },

    openSaveSearch: function (contRef) {
        this.loadCombo = contRef.getLoadButton();
        this.saveSearchButton = contRef.getSaveSearchButton();
        var me = this;
        var confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.save', 'UNI', 'Save'),
            itemId: 'saveSearchConfirmationWindow',
            green : true,
            closeAction: 'destroy',
            confirmation: function (button) {
                me.saveSearchCriteria(button, contRef);
                this.destroy();
            }
        });
        confirmationWindow.insert(1, {
            xtype: 'container',
            layout: {
                type: 'hbox',
                pack: 'end'
            },
            items: [{
                xtype: 'combobox',
                id: 'saveEntered',
                itemId:'Save-Entered',
                emptyText: Uni.I18n.translate('general.typeName', 'UNI', 'Type a name'),
                fieldLabel: Uni.I18n.translate('general.nameCombo', 'UNI', 'Name'),
                required: true,
                requiredField: true,
                allowBlank: false,
                forceSelect: true,
                typeAhead: true,
                minChars: 2,
                store: Ext.create('Uni.store.search.SaveLoad'),
                displayField: 'name',
                valueField: 'name',
                style: {
                    'margin-right': '250px',
                    'margin-top': '10px',
                    'margin-bottom': '20px'
                },
                listConfig: {
                    maxHeight: 200,
                    style: "border-radius : 4px",
                    shadow: true,
                    bodyPadding: 10,
                    margin: 0
                }
            }]
        });
        this.saveSearchWindow = confirmationWindow;
        confirmationWindow.show({
            htmlEncode: false,
            msg: Uni.I18n.translate('general.overwriteIndication', 'UNI', 'The previously saved search criteria will be overwritten by entering the same name'),
            title: Uni.I18n.translate('general.saveCriteriaTitle', 'UNI', 'Save the search criteria?')
        });

    },

    loadSearch: function (combo, value, a, contRef) {
        this.loadCombo = contRef.getLoadButton();
        this.saveSearchButton = contRef.getSaveSearchButton();
        var me = this,
            filters = me.getFilters();
        combo.selectedValue = combo.getValue();

        if(combo.nameValue !== undefined && combo.nameValue === 'delete'){
            var confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'removeSearchConfirmationWindow'
            });
            confirmationWindow.show({
                title: Ext.String.format(Uni.I18n.translate('importService.remove.title', 'UNI', 'Remove \'{0}\'?'), combo.selectedValue),
                msg: Uni.I18n.translate('importService.remove.message', 'UNI', 'This search criteria will no longer be available.'),
                fn: function (state) {
                    if (state === 'confirm') {
                        me.removeSearchCriteria(combo.selectedValue, contRef);
                        combo.nameValue = undefined;
                    }else if(state === 'cancel'){
                        combo.nameValue = undefined;
                    }
                }
            });
        }else {
            me.criteriaName = value[0].getData().name;
            if (filters && filters.length) {
                me.getSearchResultsStore().removeAll();
                me.setDomain(me.searchDomain, function () {
                    me.applyFilters();
                    var criteria = JSON.parse(value[0].data.criteria);
                    me.setFilters(criteria);
                })
            }
            else {
                var criteria = JSON.parse(value[0].data.criteria);
                me.setFilters(criteria);
            }
        }
    },

    saveSearchCriteria: function (button, contRef) {
        var me = this;
        var flag= false;
        var router = this.router;
        var name = me.saveSearchWindow.down('#Save-Entered').getValue();
        if (router && router.currentRoute == 'search') {
            Uni.util.History.setParsePath(false);
            router.getRoute('search').forward(null, Ext.apply(router.queryParams, {restore: true}));
        }
        if(name !== null &&  typeof (name !==  'undefined')) {
            Ext.Ajax.request({
                type: 'rest',
                url: "../../api/jsr/search/saveCriteria/" + name,
                method: "POST",
                async : false,
                params: {
                    filter: JSON.stringify(me.getFilters()),
                    domain: JSON.stringify(me.getDomain().id)
                },
                success: function (response) {
                    flag=true;
                    contRef.getLoadButton().getStore().load();
                    if(JSON.parse(response.responseText).status === 'Save')
                        contRef.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.saveSearch', 'UNI', 'Search criteria saved'));
                    else
                        contRef.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.updateSearch', 'UNI', 'Search criteria updated'));
                },
                failure: function (response) {
                    var errorText = Uni.I18n.translate('general.save.operation.failed', 'UNI', 'Save operation failed') + '.' + Uni.I18n.translate('error.unknownErrorOccurred', 'UNI', 'An unknown error occurred');
                    var titleText = Uni.I18n.translate('error.requestFailedConnexoKnownError', 'UNI', 'Couldn\'t perform your action'),
                        code = '';
                    if (response  && response.responseText && response.responseText.errorCode) {
                        code = response.responseText.errorCode;
                    }
                    contRef.getApplication().getController('Uni.controller.Error').showError(titleText, errorText, code);


                    }
                });
            }
        return flag;
    },

    removeSearchCriteria: function (name, contRef) {
        var me = this;
        flag= false;
        Ext.Ajax.request({
            type: 'rest',
            url: "../../api/jsr/search/searchCriteria/" + name,
            method: "DELETE",
            async : false,
            success: function (response) {
                me.clearFilters();
                var loadButtonCmp= contRef.getLoadButton();
                loadButtonCmp.clearValue();
                loadButtonCmp.getStore().load();
                contRef.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.deleteSearch', 'UNI', 'Search criteria deleted'));
            },
            failure: function (response) {
                var errorText = Uni.I18n.translate('general.remove.operation.failed', 'UNI', 'Remove operation failed') + '.' + Uni.I18n.translate('error.unknownErrorOccurred', 'UNI', 'An unknown error occurred');
                var titleText = Uni.I18n.translate('error.requestFailedConnexoKnownError', 'UNI', 'Couldn\'t perform your action'),
                    code = '';
                if (response  && response.responseText && response.responseText.errorCode) {
                    code = response.responseText.errorCode;
                }
                contRef.getApplication().getController('Uni.controller.Error').showError(titleText, errorText, code);


            }
        });
        return flag;
    }
});