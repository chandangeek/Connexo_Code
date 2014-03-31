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
        var record = button.up('form').up('form').getRecord();
        this.editUser(record);

    },

    editUser: function (record) {
        this.getApplication().getUserGroupsController().showEditOverviewWithHistory(record.get('id'));
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
            roles += currentGroups[i].data.name + '<br/>';
        }

        var detailsRoles = Ext.getCmp('els_usm_userDetailsRoles');
        detailsRoles.setValue(roles);

        form.show();
    }
});