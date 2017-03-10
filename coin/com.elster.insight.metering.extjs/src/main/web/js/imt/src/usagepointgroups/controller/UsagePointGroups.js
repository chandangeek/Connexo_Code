/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.controller.UsagePointGroups', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification'
    ],

    views: [
        'Imt.usagepointgroups.view.UsagePointGroupsGrid',
        'Imt.usagepointgroups.view.UsagePointsOfUsagePointGroupGrid',
        'Imt.usagepointgroups.view.UsagePointGroupPreview',
        'Imt.usagepointgroups.view.Details',
        'Imt.usagepointgroups.view.PreviewForm',
        'Imt.usagepointgroups.view.UsagePointGroupSetup'
    ],

    stores: [
        'Imt.usagepointgroups.store.UsagePointGroups',
        'Imt.usagepointgroups.store.UsagePointsOfUsagePointGroup'
    ],

    refs: [
        {ref: 'page', selector: 'usagepointgroup-setup'},
        {ref: 'usagePointGroupsGrid', selector: 'usagepointgroups-grid'},
        {ref: 'usagePointGroupPreviewForm', selector: 'usagepointgroup-preview-form'},
        {ref: 'usagePointGroupPreview', selector: 'usagepointgroup-preview'},        
        {ref: 'createUsagePointGroupButton', selector: '#add-usage-point-group-btn'},
        {ref: 'usagePointsOfUsagePointGroupGrid', selector: 'usagepoints-of-usagepointgroup-grid'},        
        {ref: 'removeUsagePointGroupMenuItem', selector: '#remove-usagepointgroup'},
        {ref: 'editUsagePointGroupMenuItem', selector: '#edit-usagepointgroup'},
        {ref: 'countButton', selector: 'usagepointgroup-details button[action=countUsagePointsOfGroup]'}
    ],

    init: function () {
        this.control({
            'usagepointgroups-grid': {
                selectionchange: this.previewUsagePointGroup
            },
            '#add-usage-point-group-btn': {
                click: this.showAddUsagePointGroupWizard
            },
            '#add-usage-point-group-btn-from-empty-grid': {
                click: this.showAddUsagePointGroupWizard
            },
            'usagepointgroup-action-menu': {
                click: this.chooseAction
            },
            'usagepointgroup-details #generate-report': {
                click: this.onGenerateReport
            },
            'usagepointgroup-details button[action=countUsagePointsOfGroup]': {
                click: this.getUsagePointCount
            }
        });
    },

    showAddUsagePointGroupWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('usagepoints/usagepointgroups/add').forward();
    },

    showUsagePointGroups: function () {
        var me = this,
            widget = Ext.widget('usagepointgroup-setup', {router: me.getController('Uni.controller.history.Router')}),
            addUsagePointGroupController = me.getApplication().getController('Imt.usagepointgroups.controller.AddUsagePointGroupAction');

        if (addUsagePointGroupController.router) {
            addUsagePointGroupController.router = null;
        }
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    previewUsagePointGroup: function () {
        var usagePointGroups = this.getUsagePointGroupsGrid().getSelectionModel().getSelection(),
            actionMenu = this.getUsagePointGroupPreview().down('usagepointgroup-action-menu'),
            usagePointGroup;

        if (usagePointGroups.length == 1) {
            usagePointGroup = usagePointGroups[0];
            Ext.suspendLayouts();
            this.getUsagePointGroupPreviewForm().loadRecord(usagePointGroup);
            this.getUsagePointGroupPreview().setTitle(Ext.String.htmlEncode(usagePointGroup.get('name')));
            if (actionMenu) {
                this.getUsagePointGroupPreview().down('usagepointgroup-action-menu').record = usagePointGroup;
            }
            this.updateCriteria(usagePointGroup);
            this.updateActionMenuVisibility(usagePointGroup);
            Ext.resumeLayouts(true);
        }
    },    

    updateCriteria: function (record) {
        var func = function (menuItem) {
            if (Imt.privileges.UsagePointGroup.canAdministrate()) {
                menuItem.show();
            } else if (Imt.privileges.UsagePointGroup.canAdministrate() || Imt.privileges.UsagePointGroup.canViewGroupDetails()) {
                menuItem.hide();
            }
        };

        Ext.Array.each(Ext.ComponentQuery.query('#remove-usagepointgroup'), function (item) {
            func(item);
        });
        Ext.Array.each(Ext.ComponentQuery.query('#edit-usagepointgroup'), function (item) {
            func(item);
        });
        if (!record.get('dynamic')) {
            Ext.Array.each(Ext.ComponentQuery.query('#edit-usagepointgroup'), function (editItem) {
                if (Imt.privileges.UsagePointGroup.canAdministrateUsagePointOfEnumeratedGroup()) {
                    editItem.show();
                }
            });
        }
    },

    showUsagePointGroupDetailsView: function (currentUsagePointGroupId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            router = me.getController('Uni.controller.history.Router'),
            model = me.getModel('Imt.usagepointgroups.model.UsagePointGroup'),
            addUsagePointGroupController = me.getApplication().getController('Imt.usagepointgroups.controller.AddUsagePointGroupAction'),
            service = Ext.create('Imt.service.Search', {
                router: router
            }),
            usagePointsOfGroupStore = me.getStore('Imt.usagepointgroups.store.UsagePointsOfUsagePointGroup'),
            dependenciesCounter = 2,
            onDependenciesLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    me.getApplication().fireEvent('changecontentevent', Ext.widget('usagepointgroup-details', {
                        router: router,
                        usagePointGroup: usagePointGroup,
                        service: service,
                        favoriteRecord: Ext.create('Imt.usagepointgroups.model.UsagePointGroupFavorite', {
                            id: currentUsagePointGroupId,
                            parent: {
                                id: currentUsagePointGroupId,
                                version: usagePointGroup.get('version')
                            }
                        })
                    }));
                    me.updateCriteria(usagePointGroup);
                    me.updateActionMenuVisibility(usagePointGroup);

                    service.applyState({
                        domain: 'com.elster.jupiter.metering.UsagePoint',
                        filters: [{
                            property: 'usagePointGroup',
                            value: [{
                                criteria: currentUsagePointGroupId,
                                operator: '=='
                            }]
                        }]
                    });

                    mainView.setLoading(false);
                }
            },
            usagePointGroup;

        if (addUsagePointGroupController.router) {
            addUsagePointGroupController.router = null;
        }
        usagePointsOfGroupStore.getProxy().setExtraParam('id', currentUsagePointGroupId);

        mainView.setLoading();
        service.getSearchDomainsStore().load(onDependenciesLoad);
        model.load(currentUsagePointGroupId, {
            success: function (record) {
                usagePointGroup = record;
                me.getApplication().fireEvent('loadUsagePointGroup', record);
                onDependenciesLoad();
            }
        });
    },

    getUsagePointCount: function () {
        var me = this,
            countBtn = this.getCountButton(),
            usagePointsOfGroupStore = me.getStore('Imt.usagepointgroups.store.UsagePointsOfUsagePointGroup');

        me.fireEvent('loadingcount');
        Ext.Ajax.suspendEvent('requestexception');
        countBtn.up('panel').setLoading(true);
        Ext.Ajax.request({
            url: usagePointsOfGroupStore.getProxy().url.replace('{id}', usagePointsOfGroupStore.getProxy().extraParams.id) + '/count',
            method: 'GET',
            success: function (response) {
                countBtn.setText(JSON.parse(response.responseText).numberOfSearchResults);
                countBtn.setDisabled(true);
                countBtn.up('panel').setLoading(false);
            },
            failure: function () {
                var box = Ext.create('Ext.window.MessageBox', {
                    buttons: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.close', 'IMT', 'Close'),
                            action: 'close',
                            name: 'close',
                            ui: 'remove',
                            handler: function () {
                                box.close();
                            }
                        }
                    ],
                    listeners: {
                        beforeclose: {
                            fn: function () {
                                countBtn.setDisabled(true);
                                countBtn.up('panel').setLoading(false);
                            }
                        }
                    }
                });
                box.show({
                    title: Uni.I18n.translate('general.timeOut', 'IMT', 'Time out'),
                    msg: Uni.I18n.translate('general.timeOutMessageGroups', 'IMT', 'Counting the usage point group members took too long.'),
                    modal: false,
                    ui: 'message-error',
                    icon: 'icon-warning2',
                    style: 'font-size: 34px;'
                });
            },
            callback: function () {
                Ext.Ajax.resumeEvent('requestexception');
            }
        });
    },

    updateActionMenuVisibility: function (record) {
        Ext.Array.each(Ext.ComponentQuery.query('usagepointgroup-action-menu'), function (menu) {
            Ext.suspendLayouts();
            if (Imt.privileges.UsagePointGroup.canAdministrate()) {
                menu.down('#remove-usagepointgroup').setVisible(true);
                menu.down('#edit-usagepointgroup').setVisible(true);
            } else if (Imt.privileges.UsagePointGroup.canAdministrateUsagePointOfEnumeratedGroup()) {
                if (menu.itemId != 'usagepointgroup-actioncolumn') {
                    Ext.ComponentQuery.query('uni-button-action')[0].setVisible(!record.get('dynamic'));
                }
                menu.down('#remove-usagepointgroup').setVisible(false);
                menu.down('#edit-usagepointgroup').setVisible(!record.get('dynamic'));
            }
            Ext.resumeLayouts(true);
        });
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            additionalParams = {},
            grid = this.getUsagePointGroupsGrid(),
            route;

        router.arguments.usagePointGroupId = menu.record.getId();
        switch (item.action) {
            case 'editUsagePointGroup':
                route = 'usagepoints/usagepointgroups/view/edit';
                break;
            case 'removeUsagePointGroup':
                me.removeUsagePointGroup(menu.record);
                break;
        }
        grid ? additionalParams.fromDetails = false : additionalParams.fromDetails = true;
        if (route) {
            route = router.getRoute(route);
            route.forward(router.arguments, additionalParams);
        }
    },

    removeUsagePointGroup: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            grid;

        confirmationWindow.show({
            msg: Uni.I18n.translate('usagepointgroup.remove.msg', 'IMT', 'This usage point group will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'?", [record.data.name]),
            fn: function (state) {
                if (state === 'confirm') {
                    record.destroy({
                        success: function () {
                            if (me.getPage()) {
                                grid = me.getPage().down('usagepointgroups-grid');
                                grid.down('pagingtoolbartop').totalCount = 0;
                                grid.down('pagingtoolbarbottom').resetPaging();
                                grid.getStore().load();
                            } else {
                                me.getController('Uni.controller.history.Router').getRoute('usagepoints/usagepointgroups').forward();
                            }
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('usagepointgroup.remove.success.msg', 'IMT', 'Usage point group removed'));
                        }
                    });
                }
            }
        });
    },

    onGenerateReport: function () {        
        var groupName = '',
            usagePointGroupName = Ext.ComponentQuery.query('#usageppointgroup-name'),
            reportFilter = {};

        if ((usagePointGroupName != null) && (_.isArray(usagePointGroupName))) {
            groupName = usagePointGroupName[0].getValue();
        }        
        reportFilter['search'] = false;
        reportFilter['GROUPNAME'] = groupName;        
        this.getController('Uni.controller.history.Router').getRoute('workspace/generatereport').forward(null, {
            category: 'MDM',
            filter: Ext.JSON.encode(reportFilter)
        });
    }
});

