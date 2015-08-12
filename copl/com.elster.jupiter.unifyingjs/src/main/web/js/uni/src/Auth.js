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
            if ((privilege === Ldr.store.Privileges.getAt(i).get('name') && Ext.isDefined(Ext.Ajax.defaultHeaders) && (Ext.Ajax.defaultHeaders['X-CONNEXO-APPLICATION-NAME'] === Ldr.store.Privileges.getAt(i).get('applicationName'))) ||
                (privilege === Ldr.store.Privileges.getAt(i).get('name') && !Ext.isDefined(Ext.Ajax.defaultHeaders)))
            {
                return true;
            }
        }
        return false;
    },

    hasNoPrivilege: function (privilege) {
        return !this.hasPrivilege(privilege);
    },

    hasAnyPrivilege: function (privileges) {
        var result = false;
        if (Ext.isArray(privileges)) {
            for (var i = 0; i < privileges.length; i++) {
                var privilege = privileges[i];
                if (Ext.isArray(privilege)) {
                    result = false;
                    for (var j = 0; j < privilege.length; j++) {
                        result = result || this.hasPrivilege(privilege[j]);
                    }
                    if (!result) {
                        return result;
                    }
                } else {
                    if (this.hasPrivilege(privilege)) {
                        return true;
                    }
                }
            }
        }
        return result;
    },
    checkPrivileges: function (privileges) {
        return !( (Ext.isBoolean(privileges) && !privileges) ||
        (Ext.isFunction(privileges) && !privileges()) ||
        (Ext.isArray(privileges) && !Uni.Auth.hasAnyPrivilege(privileges)) ||
        (Ext.isString(privileges) && !Uni.Auth.hasAnyPrivilege([privileges])) );
    }
});