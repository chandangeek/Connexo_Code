Ext.define('Usr.controller.UserGroups', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    stores: [
        'Groups',
        'Users'
    ],

    models: [
        'Group',
        'User'
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

    showEditOverview: function (userId) {
        var userStore = Ext.StoreManager.get('Users');
        var widget = Ext.widget('userEdit');

        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Usr.model.User').load(userId, {
            success: function (user) {
                userStore.load({
                    callback: function (store) {
                        title = Uni.I18n.translate('user.edit.with.name', 'USM', 'Edit user');
                        widget.down('form').loadRecord(user);
                        widget.down('#els_usm_userEditHeader').update('<h1>' + title + ' "' + user.get('authenticationName') + '"' + '</h1>');
                        widget.down('[name=authenticationName]').disable();
                        //widget.down('[name=description]').disable();
                        widget.down('[name=domain]').disable();
                        me.displayBreadcrumb(user.get("authenticationName"));

                        me.getGroupsStore().load(function () {
                            me.resetGroupsForRecord(user);
                        });

                        widget.setLoading(false);
                    }
                })
            }
        });

        widget.down('#cancelLink').autoEl.href = this.getApplication().getHistoryUserController().tokenizePreviousTokens();

        this.getApplication().getMainController().showContent(widget);
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
        var form = button.up('form'),
            record = form.getRecord(),
            values = form.getValues(),
            roles = this.getSelectRolesGrid().store.data.items;

        var selected = [];
        for (var i = 0; i < roles.length; i++) {
            if(roles[i].data.selected){
                selected.push(roles[i]);
            }
        }

        record.set(values);
        record.groups().removeAll();
        record.groups().add(selected);

        var me=this;
        record.save({
            success: function (record) {
                location.href = form.down('#cancelLink').autoEl.href;
            },
            failure: function(record,operation){
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.markInvalid(json.errors);
                }
            }
        });
    },

    resetGroupsForRecord: function (record) {
        var groups = this.getGroupsStore(),
            currentGroups = record.groups().data.items;

        var availableGroups = groups.data.items;

        for (var i = 0; i < currentGroups.length; i++) {
            var id = currentGroups[i].data.id;
            var result = groups.getById(id);
            if(result){
                result.data.selected = true;
            }
        }

        var selectedStore = this.getSelectRolesGrid().store;
        selectedStore.removeAll();
        selectedStore.add(availableGroups);
    }
});