Ext.define('Mtr.controller.Party', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem'
    ],

    stores: [
        'Parties',
        'InRoles',
        'Representations',
        'Roles',
        'Users'
    ],

    models: [
        'Party',
        'Role',
        'InRole',
        'Representation',
        'User'
    ],

    views: [
        'party.Overview',
        'party.Browse',
        'party.Filter',
        'party.Edit'
    ],

    refs: [
        {
            ref: 'availableRolesGrid',
            selector: 'partyEdit #availableRoles'
        },
        {
            ref: 'activeRolesGrid',
            selector: 'partyEdit #activeRoles'
        },
        {
            ref: 'availableDelegatesGrid',
            selector: 'partyEdit #availableDelegates'
        },
        {
            ref: 'activeDelegatesGrid',
            selector: 'partyEdit #activeDelegates'
        },
        {
            ref: 'filterForm',
            selector: 'partyFilter > form'
        }
    ],

    editWindow: null,

    init: function () {
        this.initMenu();

        this.control({
            'partyOverview breadcrumbTrail': {
                afterrender: this.onAfterRender
            },
            '#partyList': {
                itemdblclick: this.editParty
            },
            'partyEdit button[action=update]': {
                click: this.updateParty
            },
            'partyList button[action=save]': {
                click: this.saveParties
            },
            'partyFilter button[action=filter]': {
                click: this.filterParties
            },
            'partyFilter button[action=reset]': {
                click: this.resetFilter
            },
            '#roleActions button[action=activate]': {
                click: this.activateRole
            },
            '#roleActions button[action=deactivate]': {
                click: this.deactivateRole
            },
            '#roleActions button[action=reset]': {
                click: this.resetRoles
            },
            '#delegateActions button[action=activate]': {
                click: this.activateDelegate
            },
            '#delegateActions button[action=deactivate]': {
                click: this.deactivateDelegate
            },
            '#delegateActions button[action=reset]': {
                click: this.resetDelegates
            }
        });
    },

    onAfterRender: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Parent',
            href: '#/parent'
        });
        var breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Child 1',
            href: 'child1'
        });
        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Child 2',
            href: 'child2'
        });
        var breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Child 3',
            href: 'child3'
        });
        var breadcrumbChild4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Child 4',
            href: 'child4'
        });
        var breadcrumbChild5 = Ext.create('Uni.model.BreadcrumbItem', {
            text: 'Child 5',
            href: 'child5'
        });
        breadcrumbParent.setChild(breadcrumbChild1)
            .setChild(breadcrumbChild2)
            .setChild(breadcrumbChild3)
            .setChild(breadcrumbChild4)
            .setChild(breadcrumbChild5);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Parties',
            href: Mtr.getApplication().getHistoryPartyController().tokenizeShowOverview(),
            glyph: 'xe01c@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var widget = Ext.widget('partyOverview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    editParty: function (grid, record) {
        var me = this,
            id = record.data.id,
            party = me.getPartiesStore().getById(id);

        me.editWindow = Ext.widget('partyEdit');

        if (party !== null) {
            me.editWindow.showParty(party);

            me.resetRolesForRecord(record);
            me.resetDelegatesForRecord(record);
        }
    },

    updateParty: function () {
        var me = this,
            form = me.editWindow.getEditForm(),
            party = me.editWindow.getParty(),
            values = form.getValues();

        me.updateRoles(party);
        me.updateRepresentations(party);

        party.set(values);
        party.save({
            callback: function () {
                party.commit();
                me.editWindow.close();
            }
        });
    },

    updateRoles: function (party) {
        var me = this,
            currentInRoles = me.getInRolesStore().data.items,
            currentRoles = me.getCurrentInRoles(),
            updatedRoles = me.getActiveRolesGrid().store.data.items;

        // Check for additions or existing role.
        _.each(updatedRoles, function (updatedRole) {
            var isPhantom = _.indexOf(currentRoles, updatedRole) < 0,
                updatedInRole = null;

            if (isPhantom) {
                updatedInRole = me.createNewInRole(party.data.id, updatedRole);
            } else {
                updatedInRole = me.getInRoleForRole(currentInRoles, updatedRole);
            }

            updatedInRole.save({
                params: {
                    id: party.data.id
                }
            });
        });

        // Check for removals.
        _.each(currentRoles, function (currentRole) {
            if (_.indexOf(updatedRoles, currentRole) < 0) {
                // TODO Remove the role.
            }
        });

        // TODO Is saving in bulk even possible?
    },

    createNewInRole: function (partyId, role) {
        var minute = 1000 * 60,
            startDate = new Date(Math.floor(new Date().getTime() / minute) * minute),
            inRole = Ext.create('Mtr.model.InRole', {
                partyId: partyId,
                roleMRID: role.mRID,
                start: startDate.getTime()
            });

        inRole.phantom = true;
        inRole.setRole(role);
        return inRole;
    },

    getInRoleForRole: function (currentInRoles, updatedRole) {
        var updatedInRole = null;

        for (var i = 0; i < currentInRoles.length; i++) {
            var currentInRole = currentInRoles[i];
            if (currentInRole.data.roleMRID === updatedRole.mRID) {
                updatedInRole = currentInRole;
                break;
            }
        }

        return updatedInRole;
    },

    getCurrentInRoles: function () {
        return this.getInRolesStore().data.items
    },

    updateRepresentations: function (party) {
        var me = this,
            representationsStore = me.getRepresentationsStore(),
            activeDelegates = me.getActiveDelegatesGrid().store.data.items,
            activeRepresentations = [];

        // TODO Change the start date on the delegate to now (rounded down to the minute).

        // TODO Save the delegates to the representation store.

        // TODO Check if below comment is still valid?
        // Otherwise the back-end does not want to accept the response.
        for (var i = 0; i < activeDelegates.length; i++) {
            var delegate = activeDelegates[i];
            delete delegate.groupsStore;
        }
    },

    saveSuccess: function () {
//        alert('Saved');
    },

    saveFailed: function () {
        alert('Failed');
    },

    saveParties: function (button) {
        this.getPartiesStore().sync({
            success: this.saveSuccess,
            failure: this.saveFailed
        });
    },

    filterParties: function (button) {
        var form = this.getFilterForm();
        this.getPartiesStore().load({
            params: form.getValues()
        });
    },

    resetFilter: function (button) {
        var form = this.getFilterForm();
        form.getForm().reset();
        this.getPartiesStore().load();
    },

    activateRole: function (button) {
        var selection = this.getAvailableRolesGrid().getSelectionModel().getSelection();

        this.getAvailableRolesGrid().store.remove(selection);
        this.getActiveRolesGrid().store.add(selection);
    },

    deactivateRole: function (button) {
        var selection = this.getActiveRolesGrid().getSelectionModel().getSelection();

        this.getActiveRolesGrid().store.remove(selection);
        this.getAvailableRolesGrid().store.add(selection);
    },

    resetRoles: function (button) {
        var win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord();
        this.resetRolesForRecord(record);
    },

    resetRolesForRecord: function (record) {
        var me = this;

        me.getInRolesStore().load({
            params: {
                id: record.data.id
            },
            callback: function () {
                var roles = me.getRolesStore(),
                    currentRoles = me.getInRolesStore().data.items;

                var availableRoles = roles.data.items,
                    availableStore = me.getAvailableRolesGrid().store;

                availableStore.removeAll();
                availableStore.add(availableRoles);
                availableStore.remove(currentRoles);

                var activeRoles = [],
                    activeStore = me.getActiveRolesGrid().store;

                activeStore.removeAll();
                if (currentRoles.length > 0) {
                    // Find and add records that are currently active.
                    for (var i = 0; i < currentRoles.length; i++) {
                        var roleId = currentRoles[i].getRole().data.mRID;
                        var result = roles.getById(roleId);
                        activeRoles.push(result);
                    }
                    activeStore.add(activeRoles);
                }
            }});
    },

    activateDelegate: function (button) {
        var selection = this.getAvailableDelegatesGrid().getSelectionModel().getSelection();

        this.getAvailableDelegatesGrid().store.remove(selection);
        this.getActiveDelegatesGrid().store.add(selection);
    },

    deactivateDelegate: function (button) {
        var selection = this.getActiveDelegatesGrid().getSelectionModel().getSelection();

        this.getActiveDelegatesGrid().store.remove(selection);
        this.getAvailableDelegatesGrid().store.add(selection);
    },

    resetDelegates: function (button) {
        var win = button.up('window'),
            form = win.down('form'),
            record = form.getRecord();
        this.resetDelegatesForRecord(record);
    },

    resetDelegatesForRecord: function (record) {
        var me = this;

        me.getRepresentationsStore().load({
            params: {
                id: record.data.id
            },
            callback: function () {
                var delegates = me.getUsersStore(),
                    currentRepresentations = me.getRepresentationsStore().data.items,
                    currentDelegates = [];

                for (var i = 0; i < currentRepresentations.length; i++) {
                    var delegate = currentRepresentations[i].getAssociatedData().user;
                    currentDelegates.push(delegate);
                }

                var availableDelegates = delegates.data.items,
                    availableStore = me.getAvailableDelegatesGrid().store;

                availableStore.removeAll();
                availableStore.add(availableDelegates);
                availableStore.remove(currentDelegates);

                var activeDelegates = [],
                    activeStore = me.getActiveDelegatesGrid().store;

                activeStore.removeAll();
                if (currentDelegates.length > 0) {
                    // Find and add records that are currently active.
                    for (var i = 0; i < currentDelegates.length; i++) {
                        var delegateId = currentDelegates[i].id;
                        var result = delegates.getById(delegateId);
                        activeDelegates.push(result);
                    }
                    activeStore.add(activeDelegates);
                }
            }});
    }
})
;