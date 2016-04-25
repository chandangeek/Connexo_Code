/**
 * @class Cal.privileges.Calendar
 *
 * Class that defines privileges for Calendar
 */

Ext.define('Cal.privileges.Calendar', {
    requires: [
        'Uni.Auth'
    ],
    singleton: true,
    view: ['privilege.view.touCalendars'],

    viewPreview: ['privilege.view.touPreview'],

    all: function () {
        return Ext.Array.merge(Cal.privileges.Calendar.view, Cal.privileges.Calendar.viewPreview);
    },

    canView: function () {
        return Uni.Auth.checkPrivileges(Cal.privileges.Calendar.view);
    },

    canViewPreview: function () {
        return Uni.Auth.checkPrivileges(Cal.privileges.Calendar.viewPreview);
    }
});