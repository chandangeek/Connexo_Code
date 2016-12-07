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
        },
        {
            ref: 'usersGrid',
            selector: 'userBrowse userList'
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
            'userBrowse userDetails menuitem[action=activate]': {
                click: this.activateUserMenu
            },
            'userBrowse userList uni-actioncolumn': {
                edit: this.editUser,
                activate: this.userActivation
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
        page.down('userDetails').setTitle(Ext.String.htmlEncode(record.get('authenticationName')));
        page.down('userDetails').down('user-action-menu').record = record;
        form.loadRecord(record);
        for (var i = 0; i < currentGroups.length; i++) {
            roles += '- ' + Ext.String.htmlEncode(currentGroups[i].data.name) + '<br/>';
        }
        detailsRoles.setValue(roles);
    },

    activateUserMenu: function (button) {
        var record = button.up('#userBrowse').down('#userDetailsForm').getRecord();
        this.userActivation(record);
    },

    userActivation: function (record) {
        var me = this,
            isActive = record.get('active'),
            form = me.getUserBrowse().down('#userDetailsForm'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        Ext.Ajax.request({
            url: '/api/usr/users/' + record.get('id') + (isActive ? '/deactivate' : '/activate'),
            jsonData: record.getRecordData(),
            isNotEdit: true,
            method: 'PUT',
            success: function (response) {
                var decoded = response.responseText ? Ext.decode(response.responseText, true) : null,
                    updatedRecord = decoded && decoded.users && decoded.users.length ? decoded.users[0] : null;

                if (updatedRecord) {
                    record.beginEdit();
                    record.set(updatedRecord);
                    record.set('statusDisplay', isActive
                        ? Uni.I18n.translate('general.inactive', 'USR', 'Inactive')
                        : Uni.I18n.translate('general.active', 'USR', 'Active'));
                    record.endEdit();
                }
                if (form.rendered) {
                    form.loadRecord(record);
                }
                me.getApplication().fireEvent('acknowledge', isActive
                    ? Uni.I18n.translate('users.deactivateSuccessMsg', 'USR', 'User deactivated.')
                    : Uni.I18n.translate('users.activateSuccessMsg', 'USR', 'User activated.'));
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    }
});