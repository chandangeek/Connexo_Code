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
                activate: this.activateUser
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
        this.activateUser(record);
    },

    activateUser: function (record) {
        var me = this,
            view = me.getUserDetails(),
            isActive = record.get('active');

        record.beginEdit();
        record.set('active', !isActive);
        record.endEdit(true);

        record.save({
            success: function () {
                me.updateStatusData(record);
                if(isActive)
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('users.deactivateSuccessMsg', 'USR', 'User deactivated'));
                else
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('users.activateSuccessMsg', 'USR', 'User activated'));
            },
            failure: function (response) {
                if (response.status == 400) {
                    var errorText = Uni.I18n.translate('users.error.unknown', 'USR', 'Unknown error occurred');
                    if (!Ext.isEmpty(response.statusText)) {
                        errorText = response.statusText;
                    }
                    if (!Ext.isEmpty(response.responseText)) {
                        var json = Ext.decode(response.responseText, true);
                        if (json && json.error) {
                            errorText = json.error;
                        }
                    }
                    if(suspended)
                        me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('users.deactivate.operation.failed', 'USR', 'Deactivate operation failed'), errorText);
                    else
                        me.getApplication().getController('Uni.controller.Error').showError(Uni.I18n.translate('users.activate.operation.failed', 'USR', 'Activate operation failed'), errorText);
                }
            },
            callback: function () {
                view.setLoading(false);
            }
        });
    },
    updateStatusData: function(record)
    {
        var me = this,
            page = me.getUserBrowse(),
            form = page.down('#userDetailsForm'),
            formRecord;

        formRecord = form.getRecord();
        form.loadRecord(formRecord);
    }
});