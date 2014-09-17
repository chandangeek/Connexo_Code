/**
 * @class Uni.Auth
 *
 * Authorization class that checks whether the currently logged-in user has privileges or not.
 */
Ext.define('Uni.Auth', {
    singleton: true,
    requires: ['Uni.store.Privileges'],

    /**
     * Loads the privileges for the current user.
     *
     * @param {Function} [callback] Callback after loading
     */
    load: function (callback) {
        callback = (typeof callback !== 'undefined') ? callback : function () {
        };

        Uni.store.Privileges.load({
            callback: function () {
                callback();
            }
        });
    },

    hasPrivilege: function (privilege) {
        for (var i = 0; i < Uni.store.Privileges.getCount(); i++) {
            if (privilege === Uni.store.Privileges.getAt(i).get('name')) {
                return true;
            }
        }
        return false;
    },

    hasNoPrivilege: function (privilege) {
        return !this.hasPrivilege(privilege);
    }
});