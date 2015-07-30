Ext.define('Uni.DynamicPrivileges', {
    singleton: true,

    requires: [
        'Uni.store.DynamicPrivileges'
    ],

    getDynamicPrivilegesStore: function() {
        return Uni.store.DynamicPrivileges;
    },

    checkDynamicPrivileges: function(privilege) {
        var me = this,
            dynamicPrivilegesStore = me.getDynamicPrivilegesStore(),
            privilegeRecord;

        if (!Ext.isEmpty(privilege)) {
            privilegeRecord = dynamicPrivilegesStore.findExact('name', privilege);
            return privilegeRecord >= 0;
        } else {
            return true;
        }

    },

    loadPage: function(stores, privilege, applyMethod, router) {
        var me = this,
            dynamicPrivilegesStore = me.getDynamicPrivilegesStore(),
            storesCount = stores.length,
            loadedStoresCount = 0;

        //All dependencies (such as applyMethod and crossroad methods) should be moved to another place
        dynamicPrivilegesStore.removeAll();
        dynamicPrivilegesStore.on('privilegeStoreLoaded', function() {
            loadedStoresCount += 1;
            if (storesCount === loadedStoresCount) {
                if (me.checkDynamicPrivileges(privilege)) {
                    applyMethod();
                } else {
                    crossroads.parse("/error/notfound");
                }
            }
        }, me);

        Ext.each(stores, function(store) {
            var store = Ext.data.StoreManager.lookup(store) || Ext.create(store);

            store.getProxy().setUrl(router.arguments);
            store.load({
                callback: function() {
                    this.each(function(record) {
                        dynamicPrivilegesStore.add(record);
                    });
                    dynamicPrivilegesStore.fireEvent('privilegeStoreLoaded', me);
                }
            });

        });
    }
});
