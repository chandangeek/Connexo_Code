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
            ref: 'userBrowse',
            selector: 'userBrowse'
        },
        {
            ref: 'userDetails',
            selector: 'userBrowse userDetails'
        }
    ],

    init: function () {
        this.control({
            'userBrowse userList': {
                select: this.selectUser
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
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    editUserMenu: function (button) {
        var record = button.up('#userBrowse').down('#userDetailsForm').getRecord();
        this.editUser(record);
    },

    editUser: function (record) {
        this.getApplication().getController('Usr.controller.UserEdit').showEditOverviewWithHistory(record.get('id'));
        this.getApplication().fireEvent('editUser', record);
    },

    selectUser: function (selectionModel, record) {
        var me = this,
            page = me.getUserBrowse(),
            form = page.down('#userDetailsForm'),
            roles = '',
            currentGroups = record.groups().data.items,
            detailsRoles = form.down('[name=roles]');

        page.down('userDetails').setTitle(record.get('name'));
        form.loadRecord(record);
        for (var i = 0; i < currentGroups.length; i++) {
            roles += Ext.String.htmlEncode(currentGroups[i].data.name) + '<br/>';
        }
        detailsRoles.setValue(roles);
    }
});