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
            'user-action-menu': {
                show: this.onShowUserActionMenu
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

        page.down('userDetails').setTitle(record.get('authenticationName'));
        form.loadRecord(record);
        for (var i = 0; i < currentGroups.length; i++) {
            roles += Ext.String.htmlEncode(currentGroups[i].data.name) + '<br/>';
        }
        detailsRoles.setValue(roles);
    },

    onShowUserActionMenu: function (menu) {
        var me = this,
            page = me.getUserBrowse(),
            detailForm = page.down('#userDetailsForm'),
            activate = menu.down('#activate-user'),
            deactivate = menu.down('#deactivate-user'),
            active;

        if(menu.record)
            active = menu.record.get('active');
        else
            active = detailForm.getRecord().get('active');

        activate && activate.setVisible(!active);
        deactivate && deactivate.setVisible(active);
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
                    ? Uni.I18n.translate('users.deactivateSuccessMsg', 'USR', 'User deactivated')
                    : Uni.I18n.translate('users.activateSuccessMsg', 'USR', 'User activated'));
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    }
});