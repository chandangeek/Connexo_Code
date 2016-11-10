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
        {ref: 'usagePointGroupDetailsActionMenu', selector: '#usagepointgroup-details-action-menu'},
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
        var widget = Ext.widget('usagepointgroup-setup'),
            addUsagePointGroupController = this.getApplication().getController('Imt.usagepointgroups.controller.AddUsagePointGroupAction');

        if (addUsagePointGroupController.router) {
            addUsagePointGroupController.router = null;
        }
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    previewUsagePointGroup: function () {
        var usagePointGroups = this.getUsagePointGroupsGrid().getSelectionModel().getSelection(),
            usagePointGroup;

        if (usagePointGroups.length == 1) {
            usagePointGroup = usagePointGroups[0];
            Ext.suspendLayouts();
            this.getUsagePointGroupPreviewForm().loadRecord(usagePointGroup);
            this.getUsagePointGroupPreview().setTitle(Ext.String.htmlEncode(usagePointGroup.get('name')));
            this.getUsagePointGroupPreview().down('usagepointgroup-action-menu').record = usagePointGroup;
            this.updateCriteria(usagePointGroup);
            Ext.resumeLayouts(true);
        }
    },

    translateCriteriaName: function (criteriaName) {
        switch (criteriaName) {
            case 'mRID':
                criteriaName = Uni.I18n.translate('general.mrid', 'IMT', 'MRID');
                break;
            case 'serviceCategory':
                criteriaName = Uni.I18n.translate('general.serviceCategory', 'IMT', 'Service category');
                break;
            case 'metrologyConfiguration.name':
                criteriaName = Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration');
                break;
        }
        return criteriaName;
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
            router = me.getController('Uni.controller.history.Router'),
            model = me.getModel('Imt.usagepointgroups.model.UsagePointGroup'),
            addUsagePointGroupController = me.getApplication().getController('Imt.usagepointgroups.controller.AddUsagePointGroupAction'),
            widget,
            service,
            domainsStore;

        if (addUsagePointGroupController.router) {
            addUsagePointGroupController.router = null;
        }
        this.getUsagePointsOfUsagePointGroupStore().getProxy().setExtraParam('id', currentUsagePointGroupId);
        service = Ext.create('Imt.service.Search', {
            router: router
        });
        domainsStore = service.getSearchDomainsStore();
        domainsStore.load(function () {
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
        });
        widget = Ext.widget('usagepointgroup-details', {
            router: router,
            usagePointGroupId: currentUsagePointGroupId,
            service: service
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        model.load(currentUsagePointGroupId, {
            success: function (record) {
                Ext.suspendLayouts();
                widget.down('usagepointgroups-menu #usagepointgroups-view-link').setText(record.get('name'));
                widget.down('form').loadRecord(record);
                Ext.resumeLayouts(true);
                widget.down('usagepointgroup-action-menu').record = record;
                me.getApplication().fireEvent('loadUsagePointGroup', record);
                me.updateCriteria(record);
                me.updateActionMenuVisibility(record);
            }
        });
    },

    getUsagePointCount: function () {
        var me = this,
            countBtn = this.getCountButton(),
            usagePointsOfGroupStore = this.getUsagePointsOfUsagePointGroupStore();

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
            }
        });
    },

    updateActionMenuVisibility: function (record) {
        var actionMenu = this.getUsagePointGroupDetailsActionMenu(),
            removeItem = this.getRemoveUsagePointGroupMenuItem(),
            editItem = this.getEditUsagePointGroupMenuItem();

        Ext.suspendLayouts();
        if (Imt.privileges.UsagePointGroup.canAdministrate()) {
            actionMenu.setVisible(true);
            removeItem.setVisible(true);
            editItem.setVisible(true);
        } else if (Imt.privileges.UsagePointGroup.canViewGroupDetails()) {
            actionMenu.setVisible(false);
            removeItem.setVisible(false);
            editItem.setVisible(false);
        } else if (Imt.privileges.UsagePointGroup.canAdministrateUsagePointOfEnumeratedGroup()) {
            if (record.get('dynamic')) {
                actionMenu.setVisible(false);
                removeItem.setVisible(false);
                editItem.setVisible(false);
            } else {
                actionMenu.setVisible(true);
                removeItem.setVisible(false);
                editItem.setVisible(true);
            }
        } else {
            actionMenu.setVisible(false);
            removeItem.setVisible(false);
            editItem.setVisible(false);
        }
        Ext.resumeLayouts(true);
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

