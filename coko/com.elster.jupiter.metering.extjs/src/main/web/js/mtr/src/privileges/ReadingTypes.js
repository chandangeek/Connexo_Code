Ext.define('Mtr.privileges.ReadingTypes', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,

    view: ['privilege.view.readingType'],
    admin: ['privilege.administrate.readingType'],
    all: function () {
        return Ext.Array.merge(
            Mtr.privileges.ReadingTypes.view,
            Mtr.privileges.ReadingTypes.admin
        );
    },
    canView: function () {
        return Uni.Auth.checkPrivileges(Mtr.privileges.ReadingTypes.view);
    }
});