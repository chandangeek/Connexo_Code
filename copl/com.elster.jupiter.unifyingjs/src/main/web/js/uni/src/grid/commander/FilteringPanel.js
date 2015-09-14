/**
 * @class Uni.grid.commander.FilteringPanel
 */
Ext.define('Uni.grid.commander.FilteringPanel', {
    extend: 'Uni.grid.commander.CommanderPanel',
    xtype: 'uni-grid-commander-filteringpanel',

    items: [
        {
            xtype: 'label',
            text: 'Filters',
            width: 128
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox'
            },
            flex: 1
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.clearAll','UNI','Clear all'),
            action: 'clear'
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.reconfigureStore(me.store);
        me.initActions();
    },

    reconfigureStore: function (store) {
        var me = this;

        if (Ext.isDefined(store)) {
            me.bindStore(store);
        }
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

            me.storeListeners.beforeload = me.onBeforeLoad;

            store.on(me.storeListeners);
        } else {
            delete me.storeListeners;
        }

        me.store = store;
    },

    onBeforeLoad: function (store, options) {
        // TODO
    },

    initActions: function () {
        var me = this;

        me.down('button[action=clear]').on('click', me.clearFilters, me);
    },

    clearFilters: function () {
        this.store.fireEvent('clearfilters', this.store);
    }
});