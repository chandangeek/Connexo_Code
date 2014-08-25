
Ext.define('Uni.Auth', {
    singleton: true,
    requires: ['Uni.store.Privileges'],

    /**
     * Loads the privilegesfor the current user.
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

    hasNoPrivilege : function (privilege) {
        for (var i=0; i<Uni.store.Privileges.getCount(); i++) {
            if (privilege === Uni.store.Privileges.getAt(i).get('name')) {
                return false;
            }
        }
        return true;
    }

});