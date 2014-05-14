Ext.define('Usr.controller.User', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem',
        'Usr.controller.UserGroups'
    ],
    stores: [
        'Usr.store.Groups',
        'Usr.store.Users'
    ],

    models: [
        'Usr.model.Group',
        'Usr.model.User'
    ],

    views: [
        'Usr.view.user.Browse'
    ],

    refs: [
        {
            ref: 'userDetails',
            selector: 'userBrowse userDetails'
        }
    ],

    init: function () {
        this.control({
            'userBrowse breadcrumbTrail': {
                afterrender: this.onAfterRender
            },
            'userBrowse userList': {
                itemclick: this.selectUser
            },
            'userBrowse userDetails menuitem[action=editUser]': {
                click: this.editUserMenu
            },
            'userBrowse userList actioncolumn': {
                editUserItem: this.editUser
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('userBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    onAfterRender: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#'
        });
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.title', 'USM', 'Users'),
            href: 'users'
        });

        breadcrumbParent.setChild(breadcrumbChild);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    editUserMenu: function (button) {
        var form = this.getUserDetails().down('form'),
            record = form.getRecord();

        this.editUser(record);
    },

    editUser: function (record) {
        this.getApplication().getController('Usr.controller.UserGroups').showEditOverviewWithHistory(record.get('id'));
    },

    selectUser: function (grid, record) {
        // fill in the details panel
        var title = Uni.I18n.translate('user.user', 'USM', 'User') + ' "' + record.get('authenticationName') + '"';
        var form = this.getUserDetails();

        form.setTitle(title);
        form.loadRecord(record);

        var roles = '';
        var currentGroups = record.groups().data.items;
        for (var i = 0; i < currentGroups.length; i++) {
            roles += currentGroups[i].data.name + '<br/>';
        }

        var detailsRoles = form.down('[name=roles]');
        detailsRoles.setValue(roles);
        form.show();
    }
});