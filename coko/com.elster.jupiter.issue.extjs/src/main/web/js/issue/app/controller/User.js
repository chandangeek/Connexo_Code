Ext.define('Mtr.controller.User', {
    extend: 'Ext.app.Controller',

    stores: [
        'Groups',
        'Users'
    ],

    models: [
        'Group',
        'User'
    ],

    views: [
        'user.Browse',
        'user.Edit'
    ],

    refs: [
        {
            ref: 'availableGroupsGrid',
            selector: 'userEdit #availableGroups'
        },
        {
            ref: 'activeGroupsGrid',
            selector: 'userEdit #activeGroups'
        }
    ],

    init: function () {
        this.initMenu();

        this.control({
            '#userList': {
                itemdblclick: this.editUser
            },
            'userEdit button[action=save]': {
                click: this.updateUser
            },
            'userEdit button[action=clone]': {
                click: this.cloneUser
            },
            'userList button[action=save]': {
                click: this.saveUsers
            },
            '#groupActions button[action=activate]': {
                click: this.activateGroup
            },
            '#groupActions button[action=deactivate]': {
                click: this.deactivateGroup
            },
            '#groupActions button[action=reset]': {
                click: this.resetGroups
            }
        });
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Users',
            href: Mtr.getApplication().getHistoryUserController().tokenizeShowOverview(),
            glyph: 'xe01e@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('userBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    editUser: function (grid, record) {
        var view = Ext.widget('userEdit');
        view.down('form').loadRecord(record);
        this.resetGroupsForRecord(record);
    },
    updateUser: function (button) {
        var win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord(),
            values = form.getValues(),
            activeGroups = this.getActiveGroupsGrid().store.data.items;

        record.set(values);
        record.groups().removeAll();
        record.groups().add(activeGroups);

        win.close();
        record.save();
        record.commit();
    },
    cloneUser: function (button) {
        var me = this,
            win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord().copy(),
            values = form.getValues();

        record.set(values);
        win.close();
        record.setId(null);
        record.phantom = true;

        record.save({
            callback: function () {
                record.commit();
                me.getUsersStore().add(record);
            }
        });
    },
    saveSuccess: function () {
//        alert('Saved');
    },
    saveFailed: function () {
        alert('Failed');
    },
    saveUsers: function (button) {
        this.getUsersStore().sync({
            success: this.saveSuccess,
            failure: this.saveFailed
        });
    },
    activateGroup: function (button) {
        var selection = this.getAvailableGroupsGrid().getSelectionModel().getSelection();

        this.getAvailableGroupsGrid().store.remove(selection);
        this.getActiveGroupsGrid().store.add(selection);
    },
    deactivateGroup: function (button) {
        var selection = this.getActiveGroupsGrid().getSelectionModel().getSelection();

        this.getActiveGroupsGrid().store.remove(selection);
        this.getAvailableGroupsGrid().store.add(selection);
    },
    resetGroups: function (button) {
        var win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord();
        this.resetGroupsForRecord(record);
    },
    resetGroupsForRecord: function (record) {
        var groups = this.getGroupsStore(),
            currentGroups = record.groups().data.items;

        var availableGroups = groups.data.items,
            availableStore = this.getAvailableGroupsGrid().store;

        availableStore.removeAll();
        availableStore.add(availableGroups);
        // Removes all active groups for that user.
        availableStore.remove(currentGroups);

        var activeGroups = [],
            activeStore = this.getActiveGroupsGrid().store;

        activeStore.removeAll();
        if (currentGroups.length > 0) {
            // Find and add records that are currently active.
            for (var i = 0; i < currentGroups.length; i++) {
                var groupId = currentGroups[i].data.id;
                var result = groups.getById(groupId);
                activeGroups.push(result);
            }
            activeStore.add(activeGroups);
        }
    }
});