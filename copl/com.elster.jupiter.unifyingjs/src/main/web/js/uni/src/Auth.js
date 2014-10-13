/**
 * @class Uni.Auth
 *
 * Authorization class that checks whether the currently logged-in user has privileges or not.
 */
Ext.define('Uni.Auth', {
    singleton: true,

    requires: [
        'Ldr.store.Privileges'
    ],

    hasPrivilege: function (privilege) {
        for (var i = 0; i < Ldr.store.Privileges.getCount(); i++) {
            if (privilege === Ldr.store.Privileges.getAt(i).get('name')) {
                return true;
            }
        }
        return false;
    },

    hasNoPrivilege: function (privilege) {
        return !this.hasPrivilege(privilege);
    },

    hasAnyPrivilege: function (privileges) {
        if (Ext.isArray(privileges)) {
            for (var i = 0; i < privileges.length; i++) {
                var privilege = privileges[i];
                if (this.hasPrivilege(privilege)) {
                    return true;
                }
            }
        }
        return false;
    }
});