Ext.define('Usr.controller.User', {
    extend: 'Ext.app.Controller',
    requires: [
        'Usr.controller.UserEdit'
    ],
    stores: [
        'Usr.store.Groups',
        'Usr.store.Users'
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
            'userBrowse userList': {
                selectionchange: this.selectUser
            },
            'userBrowse userDetails menuitem[action=edit]': {
                click: this.editUserMenu
            },
            'userBrowse userList uni-actioncolumn': {
                edit: this.editUser
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('userBrowse');
        this.getApplication().getController('Usr.controller.Main').showContent(widget);
    },

    editUserMenu: function (button) {
        var record = button.up('#userBrowse').down('#userDetailsForm').getRecord();
        this.editUser(record);
    },

    editUser: function (record) {
        this.getApplication().getController('Usr.controller.UserEdit').showEditOverviewWithHistory(record.get('id'));
        this.getApplication().fireEvent('editUser', record);
    },

    selectUser: function (grid, record) {
        if(record.length > 0) {
            var detailsPanel = grid.view.up('#userBrowse').down('#userDetails'),
                form = detailsPanel.down('form');

            var title = Uni.I18n.translate('user.user', 'USM', 'User') + ' "' + record[0].get('authenticationName') + '"';
            detailsPanel.setTitle(title);
            form.loadRecord(record[0]);

            var roles = '';
            var currentGroups = record[0].groups().data.items;
            for (var i = 0; i < currentGroups.length; i++) {
                roles += currentGroups[i].data.name + '<br/>';
            }

            var detailsRoles = form.down('[name=roles]');
            detailsRoles.setValue(roles);
            detailsPanel.show();
        }
    }
});