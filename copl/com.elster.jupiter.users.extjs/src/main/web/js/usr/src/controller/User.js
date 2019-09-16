/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.controller.User', {
    extend: 'Ext.app.Controller',

    requires: [
        'Usr.controller.UserEdit',
        'Usr.service.UserDomainSearchService',
        'Usr.service.Search',
        'Usr.view.user.UserActionMenu',
        'Uni.grid.column.Action'
    ],

    stores: [
        'Usr.store.Groups',
        'Usr.store.Users',
        'Usr.store.SearchResults'
    ],

    views: [
        'Usr.view.user.BrowseUsers'
    ],

    refs: [
        {
            ref: 'userBrowse',
            selector: 'usr-search-overview'
        },
        {
            ref: 'userDetails',
            selector: 'usr-search-overview userDetails'
        },
        {
            ref: 'usersGrid',
            selector: 'usr-search-overview users-grid'
        },
        {
            ref: 'searchOverview',
            selector: 'usr-search-overview'
        },
        {
            ref: 'criteriaSelector',
            selector: 'usr-search-overview search-criteria-selector'
        },
        {
            ref: 'stickyPropertiesContainer',
            selector: 'usr-search-overview #search-criteria-sticky'
        },
        {
            ref: 'removablePropertiesContainer',
            selector: 'usr-search-overview #search-criteria-removable'
        },
        {
            ref: 'searchButton',
            selector: 'usr-search-overview button[action=search]'
        },
        {
            ref: 'clearFiltersButton',
            selector: 'usr-search-overview button[action=clearFilters]'
        },
        {
            ref: 'resultsGrid',
            selector: 'usr-search-overview uni-view-search-results'
        }
    ],

    userDomainSearchService: null,

    searchFieldsOnLoadListener: null,

    init: function () {

        var me = this,
            searchResults = Ext.getStore('Uni.store.search.Results'),
            router = me.getController('Uni.controller.history.Router');

        me.service = Ext.create('Usr.service.Search', {
            router: router
        });

        me.control({
            'usr-search-overview uni-view-search-results': {
                select: this.selectUser
            },
            'usr-search-overview userDetails menuitem[action=edit]': {
                click: this.editUserMenu
            },
            'usr-search-overview userDetails menuitem[action=activate]': {
                click: this.activateUserMenu
            },
            'usr-search-overview uni-view-search-results uni-actioncolumn': {
                edit: this.editUser,
                activate: this.userActivation
            },
            'usr-search-overview search-criteria-selector menu menucheckitem': {
                checkchange: function (field, checked) {
                    checked
                        ? me.service.addProperty(field.criteria)
                        : me.service.removeProperty(field.criteria);
                }
            },
            'usr-search-overview button[action=search]': {
                click: {
                    fn: me.service.applyFilters,
                    scope: me.service
                }
            },
            'usr-search-overview button[action=clearFilters]': {
                click: {
                    fn: me.service.clearFilters,
                    scope: me.service
                }
            },
            'usr-search-overview button[action=count]': {
                click: {
                    fn: me.service.count,
                    scope: me.service
                }
            }
        });

        searchResults.on('load', function (store, items) {
//            var grid = me.getResultsGrid();
//            var btn = grid.down('#search-bulk-actions-button');
//            btn.setDisabled(!(me.service.searchDomain && me.service.searchDomain.getId() === "com.energyict.mdc.device.data.Device" && items && items.length));
        });
    },

    showOverview: function () {
        var me = this,
            searchDomains = Ext.getStore('Uni.store.search.Domains'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('usr-search-overview', {
                itemId: 'user-browse',
                router: router,
                service: me.service
            });

        me.service.setDomain('com.elster.jupiter.users.User');

        me.getResultsGrid().getStore().on('sort', function () {
            me.getResultsGrid().setLoading(false);
        });
        me.getResultsGrid().getStore().on('beforesort', function () {
            me.getResultsGrid().setLoading(true);
        });

        me.getApplication().fireEvent('changecontentevent', widget);

        searchDomains.clearFilter(true);
        searchDomains.addFilter({property: 'application', value: 'COIN'});
        searchDomains.load({
            callback: function (records) {
                me.service.initState();
            }
        });

        me.service.setDomain('com.elster.jupiter.users.User');

        var grid = me.getResultsGrid();

        var listeners = me.service.on({
            change: me.availableClearAll,
            reset: me.availableClearAll,
            scope: me,
            destroyable: true
        });

        widget.on('destroy', function () {
            listeners.destroy();
        }, me);

    },

    showBulkAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('search/bulkaction').forward();
    },

    availableClearAll: function () {
        var me = this,
            searchOverview = me.getSearchOverview(),
            filters = me.service.getFilters();

        searchOverview.down('[action=clearFilters]').setDisabled(!(filters && filters.length));
    },

    editUserMenu: function (button) {
        var me = this;

        var record = button.up('#userDetails').down('#userDetailsForm').getRecord();
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
            currentGroups = record.raw.groups,
            detailsRoles = form.down('[name=roles]');
        page.down('userDetails').setTitle(Ext.String.htmlEncode(record.get('authenticationName')));
        page.down('userDetails').down('user-action-menu').record = record;
        form.loadRecord(record);
        for (var i = 0; i < currentGroups.length; i++) {
            roles += '- ' + Ext.String.htmlEncode(currentGroups[i].name) + '<br/>';
        }
        detailsRoles.setValue(roles);
    },

    activateUserMenu: function (button) {
        var record = button.up('#userDetails').down('#userDetailsForm').getRecord();
        this.userActivation(record);
    },

    userActivation: function (record) {
        var me = this,
            isActive = record.raw.active,
            form = me.getUserBrowse().down('#userDetailsForm'),
            viewport = Ext.ComponentQuery.query('viewport')[0];

        viewport.setLoading();
        Ext.Ajax.request({
            url: '/api/usr/users/' + record.get('id') + (isActive ? '/deactivate' : '/activate'),
            jsonData: record.raw,
            isNotEdit: true,
            method: 'PUT',
            success: function (response) {
                var decoded = response.responseText ? Ext.decode(response.responseText, true) : null,
                    updatedRecord = decoded ? decoded : null;

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