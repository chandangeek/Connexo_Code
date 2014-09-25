/**
 * @class Uni.util.Common
 *
 * This class contains the commonly used functions.
 */
Ext.define('Uni.util.Common', {
    singleton: true,

    /**
     * Performs a callback function after all required stores will be loaded. Example usage:
     *
     *     var me =this;
     *
     *     Uni.util.Common.loadNecessaryStores([
     *          'Mdc.store.Domains',
     *          'Mdc.store.Subdomains',
     *          'Mdc.store.EventsOrActions'
     *     ], function () {
     *          me.getFilterForm().loadRecord(router.filter);
     *          me.setFilterView();
     *     }, false);
     *
     * @param {String/Array} stores The stores which must be loaded.
     * @param {Function} callback The callback function.
     * @param {Number} [timeout=30000 ms] Time after which the callback will be performed regardless stores loading.
     * Pass `false` to wait until the stores will be loaded.
     */
    loadNecessaryStores: function (stores, callback, timeout) {
        var me = this,
            counter,
            timeoutId,
            check;

        if (Ext.isString(stores)) {
            stores = [stores];
        }

        counter = stores.length;

        if (timeout !== false) {
            timeoutId = setTimeout(function () {
                counter = 0;
                callback();
            }, timeout || 30000);
        }

        check = function () {
            counter--;
            if (counter === 0) {
                clearTimeout(timeoutId);
                callback();
            }
        };

        Ext.Array.each(stores, function (storeClass) {
            try{
                var store = Ext.getStore(storeClass),
                    isLoading = store.isLoading();

                if (!isLoading && store.getCount()) {
                    check();
                } else if (isLoading) {
                    store.on('load', check, me, {single: true});
                } else {
                    store.load(function () {
                        check();
                    });
                }
            } catch(e) {
                check();
                //<debug>
                console.error('\'' + storeClass + '\' not found');
                //</debug>
            }
        });
    }
});