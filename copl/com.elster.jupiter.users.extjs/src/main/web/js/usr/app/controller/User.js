Ext.define('Usr.controller.User', {
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
        'user.Browse'
    ],

    refs: [
    ],

    init: function () {
        this.initMenu();

        this.control({
            'userBrowse breadcrumbTrail': {
                afterrender: this.onAfterRender
            },
            '#userList': {
                itemclick: this.selectUser
            },
            '#userDetails menuitem[action=editUser]': {
                click: this.editUserMenu
            },
            '#userList actioncolumn': {
                editUserItem: this.editUser
            }
        });
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: Uni.I18n.translate('user.title', 'USM', 'Users'),
            href: Usr.getApplication().getHistoryUserController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('userBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
        //this.getGroupsStore().load();
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
        var record = button.up('form').up('form').getRecord();
        this.editUser(record);

    },

    editUser: function (record) {
        this.getApplication().getUserGroupsController().showOverview(record);
    },

    selectUser: function (grid, record) {
        // fill in the details panel
        var form = grid.up('panel').up('container').up('container').down('form');
        form.loadRecord(record);

        var detailsHeader = Ext.getCmp('els_usm_userDetailsHeader');
        detailsHeader.update('<h4>' + Uni.I18n.translate('user.user', 'USM', 'User') + ' "' + record.get('authenticationName') + '"' + '</h4>');

        var roles = '';
        var currentGroups = record.groups().data.items;
        for (var i = 0; i < currentGroups.length; i++) {
            roles += currentGroups[i].data.name + '\n';
        }

        var detailsRoles = Ext.getCmp('els_usm_userDetailsRoles');
        detailsRoles.setValue(roles);

        form.show();
    }
});