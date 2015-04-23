/**
 * @class Usr.privileges.Users
 *
 * Class that defines privileges for Users
 */
Ext.define('Usr.privileges.Users', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    view : ['privilege.administrate.userAndRole', 'privilege.view.userAndRole'],
    admin : ['privilege.administrate.userAndRole'],
    any: function() {
        return Ext.Array.merge(Usr.privileges.Users.view,
            Usr.privileges.Users.admin);
    },
    canView:function(){
        return Uni.Auth.checkPrivileges(Usr.privileges.Users.view);
    }
});
