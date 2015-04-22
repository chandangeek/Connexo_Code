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

    onLoad: function (store) {
        var me = this,
            params = me.getFilterParams();

        store.filterBy(params);

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
            params = me.getFilterParams();
        }

        if (me.historyEnabled) {
            me.updateHistoryState();
        }

        Ext.apply(options.params, params);
    },

    loadHistoryState: function () {
        var me = this,
            queryObject = Uni.util.QueryString.getQueryStringValues();

        for (var dataIndex in queryObject) {
            var value = queryObject[dataIndex];

            if (queryObject.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                me.setValueForDataIndex(dataIndex, value);
            }
        }
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
            params = me.getFilterParams(true),
            href = Uni.util.QueryString.buildHrefWithQueryString(params);

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

    getFilterParams: function (includeUndefined) {
        var me = this,
            params = {};

        includeUndefined = includeUndefined || false;

        me.filters.each(function (filter) {
            var dataIndex = filter.dataIndex,
                paramValue = filter.getParamValue();

            if (!includeUndefined && Ext.isDefined(paramValue) && !Ext.isEmpty(paramValue)) {
                params[dataIndex] = paramValue;
            } else {
                params[dataIndex] = paramValue;
            }
        }, me);

        return params;
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
            case 'radio':
                return 'Uni.grid.filter.Radio';
            case 'text':
                return 'Uni.grid.filter.Text';
            default:
                return undefined;
        }
    }
});