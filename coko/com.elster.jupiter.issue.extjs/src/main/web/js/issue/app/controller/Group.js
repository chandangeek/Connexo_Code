Ext.define('Mtr.controller.Group', {
    extend: 'Ext.app.Controller',
    stores: [
        'Privileges',
        'Groups'
    ],
    models: [
        'Privilege',
        'Group'
    ],
    views: [
        'group.Browse',
        'group.Edit'
    ],
    refs: [
        {
            ref: 'availablePrivilegesGrid',
            selector: 'groupEdit #availablePrivileges'
        },
        {
            ref: 'activePrivilegesGrid',
            selector: 'groupEdit #activePrivileges'
        }
    ],

    init: function () {
        this.initMenu();

        this.control({
            '#groupList': {
                itemdblclick: this.editGroup
            },
            'groupEdit button[action=save]': {
                click: this.updateGroup
            },
            'groupEdit button[action=clone]': {
                click: this.cloneGroup
            },
            'groupList button[action=save]': {
                click: this.saveGroups
            },
            '#privilegeActions button[action=activate]': {
                click: this.activatePrivilege
            },
            '#privilegeActions button[action=deactivate]': {
                click: this.deactivatePrivilege
            },
            '#privilegeActions button[action=reset]': {
                click: this.resetPrivileges
            }
        });
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Groups',
            href: Mtr.getApplication().getHistoryGroupController().tokenizeShowOverview(),
            glyph: 'xe020@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('groupBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    editGroup: function (grid, record) {
        var view = Ext.widget('groupEdit');
        view.down('form').loadRecord(record);
        this.resetPrivilegesForRecord(record);
    },
    updateGroup: function (button) {
        var win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord(),
            values = form.getValues(),
            activePrivileges = this.getActivePrivilegesGrid().store.data.items;

        record.set(values);
        record.privileges().removeAll();
        record.privileges().add(activePrivileges);

        win.close();
        record.save();
        record.commit();
    },
    cloneGroup: function (button) {
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
                me.getGroupsStore().add(record);
            }
        });
    },
    saveSuccess: function () {
        alert('Saved');
    },
    saveFailed: function () {
        alert('Failed');
    },
    saveGroups: function (button) {
        this.getGroupsStore().sync({
            success: this.saveSuccess,
            failure: this.saveFailed
        });
    },
    activatePrivilege: function (button) {
        var selection = this.getAvailablePrivilegesGrid().getSelectionModel().getSelection();

        this.getAvailablePrivilegesGrid().store.remove(selection);
        this.getActivePrivilegesGrid().store.add(selection);
    },
    deactivatePrivilege: function (button) {
        var selection = this.getActivePrivilegesGrid().getSelectionModel().getSelection();

        this.getActivePrivilegesGrid().store.remove(selection);
        this.getAvailablePrivilegesGrid().store.add(selection);
    },
    resetPrivileges: function (button) {
        var win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord();
        this.resetPrivilegesForRecord(record);
    },
    resetPrivilegesForRecord: function (record) {
        var privileges = this.getPrivilegesStore(),
            currentPrivileges = record.privileges().data.items;

        var availablePrivileges = privileges.data.items,
            availableStore = this.getAvailablePrivilegesGrid().store;

        availableStore.removeAll();
        availableStore.add(availablePrivileges);
        // Removes all active privileges for that group.
        availableStore.remove(currentPrivileges);

        var activePrivileges = [],
            activeStore = this.getActivePrivilegesGrid().store;

        activeStore.removeAll();
        if (currentPrivileges.length > 0) {
            // Find and add records that are currently active.
            for (var i = 0; i < currentPrivileges.length; i++) {
                var privilegeName = currentPrivileges[i].data.name;
                var result = privileges.getById(privilegeName);
                activePrivileges.push(result);
            }
            activeStore.add(activePrivileges);
        }
    }
});