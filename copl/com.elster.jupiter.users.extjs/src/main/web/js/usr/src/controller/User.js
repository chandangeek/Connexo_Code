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
        this.userActivation(record);
    },

    userActivation: function (record) {
        var me = this,
            isActive = record.get('active');

        if (!isActive) {
            me.activateUser(record);
        }
        else
        {
            me.deactivateUser(record);
        }


    },

    activateUser: function(record)
    {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        Ext.Ajax.request({
            url: '/api/usr/users/' + record.get('id') + '/activate',
            method: 'PUT',
            success: function () {
                me.updateStatusOnActivate(record);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('users.activateSuccessMsg', 'USR', 'User activated'));
            }

        });
        viewport.setLoading(false);

    },

    deactivateUser: function(record)
    {
        var me = this,
            viewport = Ext.ComponentQuery.query('viewport')[0],
            view = me.getUserDetails();

        viewport.setLoading();
        record.beginEdit();
        record.set('active', false);
        record.set('modifiedOn',
            Uni.DateTime.formatDateTimeLong(new Date()));
        record.endEdit(true);

        record.save({
            success: function (record) {
                me.updateUserStatus(record);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('users.deactivateSuccessMsg', 'USR', 'User deactivated'));
            },
            failure: function (response, request) {
                record.beginEdit();
                record.set('active', true);
                record.endEdit(true);

            },
            callback: function () {
                view.setLoading(false);
            }
        });
        viewport.setLoading(false);
    },
    updateStatusOnActivate: function(record)
    {
        var me = this;
        record.set('active', true);
        record.set('statusDisplay', Uni.I18n.translate('general.active', 'USR', 'Active'));
        me.updateUserStatus(record);
    },

    updateUserStatus: function(record)
    {
        var me = this,
            page = me.getUserBrowse(),
            form = page.down('#userDetailsForm');

        form.loadRecord(record);
    }

});