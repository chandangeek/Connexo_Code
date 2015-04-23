/**
 * @class Isu.privileges.Issue
 *
 * Class that defines privileges for Issue
 */
Ext.define('Isu.privileges.Issue', {
    requires:[
        'Uni.Auth'
    ],
    singleton: true,
    createRule: ['privilege.administrate.creationRule'],
    comment: ['privilege.comment.issue'],
    adminRule: ['privilege.view.assignmentRule','privilege.administrate.creationRule','privilege.view.creationRule'],
    viewRule: ['privilege.view.assignmentRule'],
    adminCreateRule: ['privilege.administrate.creationRule','privilege.view.creationRule'],
    assign: ['privilege.assign.issue'],
    close: ['privilege.close.issue'],
    commentOrAssing: ['privilege.comment.issue','privilege.assign.issue'],
    notify: ['privilege.action.issue'],
    adminDevice: ['privilege.comment.issue','privilege.close.issue','privilege.assign.issue','privilege.action.issue'],
    viewAdminDevice: ['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'],

    any: function() {
        return Ext.Array.merge(Isu.privileges.Issue.createRule, Isu.privileges.Issue.comment,Isu.privileges.Issue.adminRule,
            Isu.privileges.Issue.viewRule, Isu.privileges.Issue.adminCreateRule,Isu.privileges.Issue.assign,Isu.privileges.Issue.close,Isu.privileges.Issue.notify,
            Isu.privileges.Issue.adminDevice,Isu.privileges.Issue.viewAdminDevice);

    },

    canComment: function(){
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.comment );
    },

    canAdminRule: function(){
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.adminRule);
    },

    canViewRule: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.viewRule);
    },

    canAdminCreateRule: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.adminCreateRule);
    },

    canViewAdminDevice: function() {
        return Uni.Auth.checkPrivileges(Isu.privileges.Issue.viewAdminDevice);
    }
});
