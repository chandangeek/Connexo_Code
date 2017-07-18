/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.FilterPanelTop
 */
Ext.define('Uni.grid.FilterPanelTop', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-grid-filterpaneltop',
    ui: 'filter',
    margin: '0 0 10 0',

    requires: [
        'Uni.grid.filtertop.Base',
        'Uni.grid.filtertop.Checkbox',
        'Uni.grid.filtertop.ComboBox',
        'Uni.grid.filtertop.Date',
        'Uni.grid.filtertop.Interval',
        'Uni.grid.filtertop.Radio',
        'Uni.grid.filtertop.Text',
        'Uni.grid.filtertop.DateTime',
        'Uni.grid.filtertop.DateTimeSelect',
        'Uni.grid.filtertop.Duration',
        'Uni.grid.filtertop.NoUi',
        'Uni.grid.filtertop.ClosableCombobox',
        'Uni.grid.filtertop.DueDate',
        'Uni.grid.filtertop.Numeric',
        'Uni.grid.filtertop.ReadingQuality'
    ],

    /**
     *
     */
    store: new Ext.data.ArrayStore(),

    /**
     *
     */
    filters: [],

    /**
     * @cfg {Boolean} local
     * Whether to do local filtering or not. Defaults to false.
     */
    local: false,

    /**
     * @cfg {Boolean} historyEnabled
     * Updates the querystring parameters in the URL based on the filters. Defaults to true.
     */
    historyEnabled: true,

    /**
     *
     */
    filterObjectEnabled: true,

    /**
     *
     */
    filterObjectParam: 'filter',

    layout: {
        type: 'column',
        tdAttrs: {
            style: 'padding: 10px;'
        }
    },

    padding: '16 16 0 16',

    defaults: {
        labelAlign: 'top',
        padding: '0 16 16 0'
    },

    items: [],

    hasDefaultFilters: false,

    additionalCmps: [],

    actionButtons:[
        {
            xtype: 'button',
            ui: 'action',
            text: Uni.I18n.translate('general.apply', 'UNI', 'Apply'),
            action: 'applyAll',
            itemId: 'filter-apply-all'
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.clearAll', 'UNI', 'Clear all'),
            action: 'clearAll',
            itemId: 'filter-clear-all',
            disabled: true
        }
    ],

    initComponent: function () {
        var me = this,
            store = Ext.getStore(me.store) || Ext.create(me.store);

        me.noUiFilters = [];

        me.dockedItems = [
            {
                xtype: 'container',
                dock: 'right',
                layout: {
                    type: 'hbox'
                },
                items: me.additionalCmps.concat(me.actionButtons)
            }
        ];

        me.callParent(arguments);

        me.reconfigureStore(store);
        me.initActions();

        Uni.util.QueryString.on('querystringchanged', me.onQueryStringChanged, me);
    },

    onDestroy: function () {
        var me = this;

        Uni.util.QueryString.un('querystringchanged', me.onQueryStringChanged, me);
        if (me.store && me.storeListeners) {
            me.store.un(me.storeListeners);
        }
        me.callParent(arguments);
    },

    onQueryStringChanged: function (queryString) {
        this.applyQueryObject(Uni.util.QueryString.getQueryStringValues(false), true);
    },

    applyQueryObject: function (queryObject, doApply) {
        var me = this;
        // Adapt the filters visually
        if (Ext.isArray(me.filters.items)) {
            Ext.Array.each(me.filters.items, function (filter) {
                if (filter && filter.dataIndex && queryObject[filter.dataIndex]) {
                    filter.setFilterValue(queryObject[filter.dataIndex]);
                } else {
                    filter.resetValue();
                }
            }, me);

            if (doApply) {
                me.applyFilters();
            }
        }
    },

    reconfigureStore: function (store) {
        var me = this;

        if (Ext.isDefined(store)) {
            me.bindStore(store);
        }

        me.createFilters();

        if (me.historyEnabled) {
            me.loadHistoryState();
        }
    },

    createFilters: function () {
        var me = this,
            filters = me.filters;

        Ext.suspendLayouts();

        me.removeAll();
        me.filters = me.createFiltersCollection();

        if (filters) {
            if (!Ext.isArray(filters)) {
                filters = [filters];
            }

            Ext.Array.each(filters, function (filter) {
                me.addFilter(filter);
            }, me);
        }

        Ext.resumeLayouts();
    },

    initActions: function () {
        var me = this,
            applyButton = me.down('button[action=applyAll]'),
            clearButton = me.down('button[action=clearAll]');

        applyButton.on('click', me.applyFilters, me);
        clearButton.on('click', me.clearFilters, me);
    },

    applyFilters: function () {
        var me = this,
            pagingToolbarTop = Ext.Array.findBy(Ext.ComponentQuery.query('pagingtoolbartop'), function (toolbar) {
                return toolbar.store.$className === me.store.$className;
            }),
            pagingToolbarBottom = Ext.Array.findBy(Ext.ComponentQuery.query('pagingtoolbarbottom'), function (toolbar) {
                return toolbar.store.$className === me.store.$className;
            });

        if (pagingToolbarTop) {
            pagingToolbarTop.resetPaging();
        }
        if (pagingToolbarBottom) {
            pagingToolbarBottom.resetPaging();
        }
        if (Ext.isDefined(me.store)) {
            me.store.load();
        }
    },

    clearFilters: function () {
        var me = this;

        me.filters.each(function (filter) {
            filter.resetValue();
        }, me);

        me.applyFilters();
    },

    clearFilter: function (dataIndex) {
        var me = this;

        me.filters.each(function (filter) {
            if (filter.dataIndex === dataIndex) {
                filter.resetValue();
                return false;
            }
        }, me);

        me.applyFilters();
    },

    bindStore: function (store) {
        var me = this;

        // Unbind from the old store.
        if (me.store && me.storeListeners) {
            me.store.un(me.storeListeners);
        }

        // Set up the correct listeners.
        if (store) {
            me.storeListeners = {
                scope: me
            };

            me.storeListeners.clearfilters = me.onClearFilters;
            me.storeListeners.removefilter = me.onRemoveFilter;

            if (me.local) {
                me.storeListeners.load = me.onLoad;
            } else {
                me.storeListeners['before' + (store.buffered ? 'prefetch' : 'load')] = me.onBeforeLoad;
            }

            store.on(me.storeListeners);
        } else {
            delete me.storeListeners;
        }

        me.store = store;
    },

    onClearFilters: function (store) {
        this.clearFilters();
    },

    onRemoveFilter: function (store, dataIndex) {
        var me = this;
        me.clearFilter(dataIndex);
    },

    onLoad: function (store) {
        var me = this,
            params = me.getFilterParams();

        // TODO Support local filtering.

        if (me.historyEnabled) {
            me.updateHistoryState();
        }
    },

    onBeforeLoad: function (store, options) {
        var me = this,
            params = {};

        options.params = options.params || {};

        // Memory proxy.
        if (me.hasActiveFilter()) {
            var tempParams = me.getFilterParams(false, !me.filterObjectEnabled);

            if (me.filterObjectEnabled) {
                params[me.filterObjectParam] = me.createFiltersObject(tempParams);
            } else {
                params = tempParams;
            }
        }

        if (me.historyEnabled) {
            me.updateHistoryState();
        }

        Ext.apply(options.params, params);
        me.enableClearAll(Ext.decode(options.params.filter, true) || []);
    },

    enableClearAll: function (filters) {
        var me = this;

        me.down('button[action=clearAll]').setDisabled(!Ext.Array.filter(filters, function (filter) {
            return !Ext.Array.contains(me.noUiFilters, filter.property);
        }).length);
    },

    createFiltersObject: function (params) {
        var result = [];

        for (var dataIndex in params) {
            var value = params[dataIndex];

            if (params.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                var filter = {
                    property: dataIndex,
                    value: value
                };

                result.push(filter);
            }
        }

        return Ext.encode(result);
    },

    loadHistoryState: function () {
        var me = this,
            queryObject = Uni.util.QueryString.getQueryStringValues(false),
            objectQueue = {};

        for (var dataIndex in queryObject) {
            var value = queryObject[dataIndex];

            if (queryObject.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                if (dataIndex.indexOf('.') >= 0) {
                    var tempQueue = {};
                    me.addValueToObj(tempQueue, dataIndex, value);
                    Ext.merge(objectQueue, tempQueue);
                } else {
                    me.setValueForDataIndex(dataIndex, value);
                }
            }
        }

        // Set object values.
        for (var objectIndex in objectQueue) {
            var objectValue = objectQueue[objectIndex];
            if (objectQueue.hasOwnProperty(objectIndex) && Ext.isDefined(objectValue) && !Ext.isEmpty(objectValue)) {
                me.setValueForDataIndex(objectIndex, objectValue);
            }
        }
    },

    addValueToObj: function (obj, index, value) {
        // Separate each step in the "path".
        var path = index.split(".");

        // Loop through each part of the path adding to obj.
        for (var i = 0, tmp = obj; i < path.length - 1; i++) {
            tmp = tmp[path[i]] = {};
        }
        // At the end of the chain add the value in.
        tmp[path[i]] = value;
    },

    setValueForDataIndex: function (dataIndex, value) {
        var me = this;

        me.filters.each(function (filter) {
            if (filter.dataIndex === dataIndex) {
                filter.setFilterValue(value);
                return false;
            }
        }, me);
    },

    updateHistoryState: function () {
        var me = this,
            params = me.getFilterParams(true, true),
            href = Uni.util.QueryString.buildHrefWithQueryString(params, false);
        if (location.href !== href) {
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            if (Uni.util.QueryString.getQueryString()) {
                location.replace(href);
            } else {
                if (me.hasDefaultFilters) {
                    location.replace(href);
                } else {
                    location.href = href;
                }
            }
            me.fireEvent('change', params);
        }
    },

    hasActiveFilter: function () {
        var me = this,
            result = false;

        me.filters.each(function (filter) {
            if (filter.active) {
                result = true;
                return false;
            }
        });

        return result;
    },

    getFilterParams: function (includeUndefined, flattenObjects) {
        var me = this,
            params = {};

        includeUndefined = includeUndefined || false;
        flattenObjects = flattenObjects || false;

        me.filters.each(function (filter) {

            if (!flattenObjects && Ext.isDefined(filter.applyParamValue)) {
                filter.applyParamValue(params, includeUndefined, flattenObjects);
            } else {
                var dataIndex = filter.dataIndex,
                    paramValue = filter.getParamValue();

                if(includeUndefined) {
                    params[dataIndex] = paramValue;
                } else if (Ext.isDefined(paramValue) && !Ext.isEmpty(paramValue)) {
                    params[dataIndex] = paramValue;
                }

                if (flattenObjects && Ext.isObject(paramValue)) {
                    me.populateParamsFromObject(params, dataIndex, paramValue);
                }
            }
        }, me);
        return params;
    },

    getFilterDisplayParams: function (includeUndefined, flattenObjects) {
        var me = this,
            params = {};

        includeUndefined = includeUndefined || false;
        flattenObjects = flattenObjects || false;

        me.filters.each(function (filter) {
            if (!flattenObjects && Ext.isDefined(filter.applyParamValue)) {
                filter.applyParamValue(params, includeUndefined, flattenObjects);
            } else {
                var dataIndex = filter.dataIndex,
                    paramValue = typeof filter.getParamDisplayValue == 'function' ?
                        filter.getParamDisplayValue() :
                        filter.getParamValue();

                if (!includeUndefined && Ext.isDefined(paramValue) && !Ext.isEmpty(paramValue)) {
                    params[dataIndex] = paramValue;
                } else {
                    params[dataIndex] = paramValue;
                }

                if (flattenObjects && Ext.isObject(paramValue)) {
                    me.populateParamsFromObject(params, dataIndex, paramValue);
                }
            }
        }, me);
        return params;
    },

    populateParamsFromObject: function (params, dataIndex, paramValue) {
        var me = this;

        for (var index in paramValue) {
            var value = paramValue[index];

            if (paramValue.hasOwnProperty(index) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                if (Ext.isObject(value)) {
                    me.populateParamsFromObject(params, dataIndex + '.' + index, value);
                } else {
                    params[dataIndex + '.' + index] = value;
                    delete params[dataIndex];
                }
            }
        }
    },

    createFiltersCollection: function () {
        return Ext.create('Ext.util.MixedCollection', false, function (o) {
            return o ? o.dataIndex : null;
        });
    },

    addFilter: function (filter) {
        var me = this,
            component = me.createFilter(filter);

        if (Ext.isDefined(component)) {
            me.filters.add(component);
            if (filter.type !== 'noui') {
                me.add(component);
            } else {
                me.noUiFilters.push(filter.dataIndex);
            }
            component.on('filterupdate', me.applyFilters, me);
        }
        return component
    },

    createFilter: function (filter) {
        var me = this,
            type = filter.type ? me.getFilterType(filter.type) : filter.xtype,
            widget = undefined,
            store = undefined;

        if (Ext.isDefined(type)) {
            delete filter.xtype;
            widget = Ext.create(type, Ext.applyIf(filter, me.defaults));
            store = widget.store;

            if (Ext.isDefined(store) && store.isStore && !store.isLoading()) {
                if (!filter.hasOwnProperty('loadStore') || Boolean(filter.loadStore)) {
                    store.load();
                }
            }

            return widget;
        }
        return undefined;
    },

    getFilterByItemId: function (itemId) {
        var me = this,
            resultFilter;
        Ext.each(me.filters.items, function (filter) {
            if (filter.itemId === itemId) {
                resultFilter = filter;
                return false;
            }
        });
        return resultFilter;
    },

    getFilterType: function (type) {
        switch (type) {
            case 'checkbox':
                return 'Uni.grid.filtertop.Checkbox';
            case 'combobox':
                return 'Uni.grid.filtertop.ComboBox';
            case 'closablecombobox':
                return 'Uni.grid.filtertop.ClosableCombobox';
            case 'date':
                return 'Uni.grid.filtertop.Date';
            case 'interval':
                return 'Uni.grid.filtertop.Interval';
            case 'radio':
                return 'Uni.grid.filtertop.Radio';
            case 'text':
                return 'Uni.grid.filtertop.Text';
            case 'datetime':
                return 'Uni.grid.filtertop.DateTime';
            case 'datetimeselect':
                return 'Uni.grid.filtertop.DateTimeSelect';
            case 'duration':
                return 'Uni.grid.filtertop.Duration';
            case 'noui':
                return 'Uni.grid.filtertop.NoUi';
            case 'dueDate':
                return 'Uni.grid.filtertop.DueDate';
            case 'numeric':
                return 'Uni.grid.filtertop.Numeric';
            case 'readingquality':
                return 'Uni.grid.filtertop.ReadingQuality';
            default:
                return undefined;
        }
    }
});