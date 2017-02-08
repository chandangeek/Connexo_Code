/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.FilterPanel
 */
Ext.define('Uni.grid.FilterPanel', {
    extend: 'Ext.panel.Panel',
    xtype: 'uni-grid-filterpanel',
    ui: 'filter',

    title: Uni.I18n.translate('uni.grid.filterpanel.title', 'UNI', 'Filters'),

    requires: [],

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
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        labelAlign: 'top'
    },

    items: [],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    ui: 'action',
                    text: Uni.I18n.translate('general.apply', 'UNI', 'Apply'),
                    action: 'apply'
                },
                {
                    text: Uni.I18n.translate('general.clearAll', 'UNI', 'Clear all'),
                    action: 'clear'
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this,
            store = Ext.getStore(me.store) || Ext.create(me.store);

        me.callParent(arguments);

        me.reconfigureStore(store);
        me.initActions();
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
            applyButton = me.down('button[action=apply]'),
            clearButton = me.down('button[action=clear]');

        applyButton.on('click', me.applyFilters, me);
        clearButton.on('click', me.clearFilters, me);
    },

    applyFilters: function () {
        var me = this;

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
                params[me.filterObjectParam] = Ext.encode(tempParams);
            } else {
                params = tempParams;
            }
        }

        if (me.historyEnabled) {
            me.updateHistoryState();
        }

        Ext.apply(options.params, params);
    },

    loadHistoryState: function () {
        var me = this,
            queryObject = Uni.util.QueryString.getQueryStringValues(),
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
            Uni.util.History.suspendEventsForNextCall();
            location.href = href;
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
            var dataIndex = filter.dataIndex,
                paramValue = filter.getParamValue();

            if (!includeUndefined && Ext.isDefined(paramValue) && !Ext.isEmpty(paramValue)) {
                params[dataIndex] = paramValue;
            } else {
                params[dataIndex] = paramValue;
            }

            if (flattenObjects && Ext.isObject(paramValue)) {
                me.populateParamsFromObject(params, dataIndex, paramValue);
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
            me.add(component);

            component.on('filterupdate', me.applyFilters, me);
        }
    },

    createFilter: function (filter) {
        var me = this,
            type = filter.type ? me.getFilterType(filter.type) : filter.xtype;

        if (Ext.isDefined(type)) {
            delete filter.xtype;
            return Ext.create(type, Ext.applyIf(filter, me.defaults));
        }
        return undefined;
    },

    getFilterType: function (type) {
        switch (type) {
            case 'checkbox':
                return 'Uni.grid.filter.Checkbox';
            case 'combobox':
                return 'Uni.grid.filter.ComboBox';
            case 'date':
                return 'Uni.grid.filter.Date';
            case 'interval':
                return 'Uni.grid.filter.Interval';
            case 'radio':
                return 'Uni.grid.filter.Radio';
            case 'text':
                return 'Uni.grid.filter.Text';
            default:
                return undefined;
        }
    }
});