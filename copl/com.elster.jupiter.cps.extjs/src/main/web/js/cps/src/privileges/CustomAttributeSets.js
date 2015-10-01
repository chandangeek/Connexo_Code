/**
 * @class Cps.privileges.CustomAttributeSets
 *
 * Class that defines privileges for Custom attribute sets
 */
Ext.define('Cps.privileges.CustomAttributeSets', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    view: ['cps.privilege.view.privileges'],
    admin: ['cps.privilege.administer.privileges'],
    all: function () {
        return Ext.Array.merge(
            Cps.privileges.CustomAttributeSets.view,
            Cps.privileges.CustomAttributeSets.admin
        );
    }
});