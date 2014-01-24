/**
 * @class Uni.override.StoreOverride
 *
 * Stops ExtJS from overriding the storeId property when using the stores property in a controller.
 */
Ext.define('Uni.override.StoreOverride', {
    override: 'Ext.app.Application',

    getStore: function (name) {
        if (Ext.isEmpty(this.self.storeIdMap)) {
            this.self.storeIdMap = {};
        }

        var storeName = this.self.storeIdMap[name],
            store = null;

        if (storeName) {
            store = Ext.StoreManager.get(storeName);
        }
        if (!store) {
            store = Ext.create(this.getModuleClassName(name, 'store'));
            this.self.storeIdMap[name] = store.storeId;
        }

        return store;
    }
});