Ext.define('Usr.controller.UserEdit', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    stores: [
        'Usr.store.Groups',
        'Usr.store.Users',
        'Usr.store.UserDirectories'
    ],

    models: [
        'Usr.model.Group',
        'Usr.model.User',
        'Usr.model.UserDirectory'
    ],

    views: [
        'Usr.view.user.Edit'
    ],

    refs: [
        {
            ref: 'selectRolesGrid',
            selector: 'userEdit #selectRoles'
        },
        {
            ref: 'breadCrumbs',
            selector: 'breadcrumbTrail'
        }
    ],

    init: function () {
        this.control({
            'userEdit button[action=save]': {
                click: this.updateUser
            },
            'userEdit button[action=cancel]': {
                click: this.back
            }
         });
    },

    showEditOverviewWithHistory: function(groupId) {
        location.href = '#usermanagement/users/' + groupId + '/edit';
    },

    backUrl: null,

    back: function() {
        location.href = this.backUrl;
    },

    /**
     * todo: this method should be refactored.
     * @param userId
     */
    showEditOverview: function (userId) {
        var widget = Ext.widget('userEdit'),
            panel = widget.getCenterContainer().items.getAt(0);

        this.backUrl = this.getApplication().getController('Usr.controller.history.UserManagement').tokenizePreviousTokens();

        this.getApplication().getController('Usr.controller.Main').showContent(widget);
        widget.hide();
        widget.setLoading(true);

        var me = this;
        Ext.ModelManager.getModel('Usr.model.User').load(userId, {
            success: function (user) {
                var title = Uni.I18n.translate('user.edit.with.name', 'USM', 'Edit user');
                panel.setTitle(title + ' "' + user.get('authenticationName') + '"');

                panel.down('[name=authenticationName]').disable();
                panel.down('[name=domain]').disable();

                Ext.StoreManager.get('Usr.store.Groups').load(function () {
                    panel.down('form').loadRecord(user);

                    Ext.ModelManager.getModel('Usr.model.UserDirectory').load(user.get('domain'), {
                        callback: function (domain) {
                            if(!domain.get('manageGroupsInternal')){
                                panel.down('[itemId=selectRoles]').disable();
                            }

                            me.displayBreadcrumb(user.get("authenticationName"));
                            widget.setLoading(false);
                            widget.show();
                        }
                    });
                });
            }
        });
    },

    displayBreadcrumb: function (userName) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#usermanagement'
        });
        var breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.title', 'USM', 'Users'),
            href: 'users'
        });

        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.edit.with.name', 'USM', 'Edit user') + ' "' + userName + '"'
        });

        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    updateUser: function (button) {
        var me = this;
        var form = button.up('form');

        form.updateRecord();
        form.getRecord().save({
            success: function (record) {
                me.back();
            },
            failure: function(record,operation){
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.markInvalid(json.errors);
                }
            }
        });
    }
});