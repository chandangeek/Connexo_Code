/**
 * @class Uni.store.Translations
 */
Ext.define('Uni.store.Privileges', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.Privilege',
    storeId: 'userPrivileges',
    singleton: true,
    autoLoad: false,
    clearOnPageLoad: false,
    clearRemovedOnLoad: false,
    remoteFilter: false
    }
);
