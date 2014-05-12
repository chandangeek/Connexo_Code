Ext.define('Usr.controller.UserGroups', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    stores: [
        'Usr.store.Groups',
        'Users',
        'UserDirectories'
    ],

    models: [
        'Group',
        'User',
        'UserDirectory'
    ],

    views: [
        'user.Edit'
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
            }
         });
    },

    showEditOverviewWithHistory: function(groupId) {
        location.href = '#users/' + groupId + '/edit';
    },

    /**
     * todo: this method should be refactored.
     * @param userId
     */
    showEditOverview: function (userId) {
        var userStore = Ext.StoreManager.get('Users');
        var widget = Ext.widget('userEdit');
        var panel = widget.getCenterContainer().items.getAt(0);

//        widget.down('#cancelLink').href = this.getApplication().getController('Usr.controller.history.User').tokenizePreviousTokens();

        widget.hide();
        widget.setLoading(true);

        var me = this;
        Ext.ModelManager.getModel('Usr.model.User').load(userId, {
            success: function (user) {
                userStore.load({
                    callback: function (store) {
                        var title = Uni.I18n.translate('user.edit.with.name', 'USM', 'Edit user');

                        panel.setTitle(title + ' "' + user.get('authenticationName') + '"');
                        widget.down('[name=authenticationName]').disable();
                        widget.down('[name=domain]').disable();

                        me.getStore('Usr.store.Groups').load(function () {
                            widget.down('form').loadRecord(user);

                            Ext.ModelManager.getModel('Usr.model.UserDirectory').load(user.get('domain'), {
                                callback: function (domain) {
                                    if(!domain.get('manageGroupsInternal')){
                                        widget.down('[itemId=selectRoles]').disable();
                                    }

                                    me.getApplication().getController('Usr.controller.Main').showContent(widget);
                                    me.displayBreadcrumb(user.get("authenticationName"));

                                    widget.setLoading(false);
                                    widget.show();
                                }
                            });
                        });
                    }
                })
            }
        });
    },

    displayBreadcrumb: function (userName) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#'
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
        var form = button.up('form');
        debugger;
        form.updateRecord();

        form.getRecord().save({
            success: function (record) {
                location.href = form.down('#cancelLink').href;
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