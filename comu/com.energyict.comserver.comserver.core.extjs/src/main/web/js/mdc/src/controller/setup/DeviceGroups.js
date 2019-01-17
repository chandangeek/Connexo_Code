/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.DeviceGroups', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.view.setup.devicegroup.DeviceGroupsGrid',
        'Mdc.view.setup.devicegroup.DevicesOfDeviceGroupGrid',
        'Mdc.view.setup.devicegroup.DeviceGroupSetup',
        'Mdc.view.setup.devicegroup.DeviceGroupPreview',
        'Mdc.view.setup.devicegroup.Details',
        'Mdc.view.setup.devicegroup.PreviewForm',
        'Mdc.store.DevicesOfDeviceGroup'
    ],
    views: [
        'setup.devicegroup.DeviceGroupsGrid',
        'setup.devicegroup.DevicesOfDeviceGroupGrid',
        'setup.devicegroup.DeviceGroupPreview',
        'setup.devicegroup.Details',
        'setup.devicegroup.PreviewForm'
    ],

    stores: [
        'DeviceGroups',
        'DevicesOfDeviceGroup'
    ],

    mixins: [],


    refs: [
        {ref: 'page', selector: 'deviceGroupSetup'},
        {ref: 'deviceGroupsGrid', selector: '#deviceGroupsGrid'},
        {ref: 'deviceGroupPreviewForm', selector: '#deviceGroupPreviewForm'},
        {ref: 'deviceGroupPreview', selector: '#deviceGroupPreview'},
        {ref: 'searchCriteriaContainer', selector: '#searchCriteriaContainer'},
        {ref: 'createDeviceGroupButton', selector: '#createDeviceGroupButton'},
        {ref: 'devicesOfDeviceGroupGrid', selector: '#allDevicesOfDeviceGroupGrid'},
        {ref: 'deviceGroupDetailsActionMenu', selector: '#deviceGroupDetailsActionMenu'},
        {ref: 'removeDeviceGroupMenuItem', selector: '#remove-device-group'},
        {ref: 'editDeviceGroupMenuItem', selector: '#edit-device-group'},
        {ref: 'countButton', selector: 'group-details button[action=countDevicesOfGroup]'}
    ],

    init: function () {
        this.control({
            '#deviceGroupsGrid': {
                selectionchange: this.previewDeviceGroup
            },
            '#createDeviceGroupButton': {
                click: this.showAddDeviceGroupWizard
            },
            '#createDeviceGroupButtonFromEmptyGrid': {
                click: this.showAddDeviceGroupWizard
            },
            'device-group-action-menu': {
                click: this.chooseAction
            },
            'group-details #generate-report': {
                click: this.onGenerateReport
            },
            'group-details button[action=countDevicesOfGroup]': {
                click: this.getDeviceCount
            }
        });
    },

    showAddDeviceGroupWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups/add').forward();
    },

    showDeviceGroups: function () {
        var widget = Ext.widget('deviceGroupSetup');
        if (this.getApplication().getController('Mdc.controller.setup.AddDeviceGroupAction').router) {
            this.getApplication().getController('Mdc.controller.setup.AddDeviceGroupAction').router = null;
        }
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    previewDeviceGroup: function (grid, record) {
        var deviceGroups = this.getDeviceGroupsGrid().getSelectionModel().getSelection();
        if (deviceGroups.length == 1) {
            this.getDeviceGroupPreview().down('uni-button-action').setVisible(Mdc.privileges.DeviceGroup.canAdministrateDeviceOfEnumeratedGroup() || Mdc.privileges.DeviceGroup.canAdministrateDeviceOfEnumeratedGroup())

            var deviceGroup = deviceGroups[0];
            this.getDeviceGroupPreviewForm().loadRecord(deviceGroup);
            this.getDeviceGroupPreview().setTitle(Ext.String.htmlEncode(deviceGroup.get('name')));
            this.getDeviceGroupPreview().down('device-group-action-menu').record = deviceGroup;
            this.updateCriteria(deviceGroup);
        }
    },

    translateCriteriaName: function (criteriaName) {
        if (criteriaName == 'deviceConfiguration.deviceType.name') {
            criteriaName = Uni.I18n.translate('general.deviceType', 'MDC', 'Device type')
        }
        else if (criteriaName == 'mRID') {
            criteriaName = Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID')
        }
        else if (criteriaName == 'serialNumber') {
            criteriaName = Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number')
        }
        else if (criteriaName == 'deviceConfiguration.name') {
            criteriaName = Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration')
        }
        return criteriaName;
    },

    back: function () {
        location.href = "#devices";
    },

    updateCriteria: function (record) {
        var me = this,
            func = function (menuItem) {
                if (Mdc.privileges.DeviceGroup.canAdministrateDeviceGroup()) {
                    menuItem.show();
                } else if (Mdc.privileges.DeviceGroup.canAdministrateDeviceGroup() || Mdc.privileges.DeviceGroup.canViewGroupDetails()) {
                    menuItem.hide();
                }
            };

        Ext.Array.each(Ext.ComponentQuery.query('#remove-device-group'), function (item) {
            func(item);
        });
        Ext.Array.each(Ext.ComponentQuery.query('#edit-device-group'), function (item) {
            func(item);
        });
        if (!record.get('dynamic')) {
            Ext.Array.each(Ext.ComponentQuery.query('#edit-device-group'), function (item) {
                if (Mdc.privileges.DeviceGroup.canAdministrateDeviceOfEnumeratedGroup()) {
                    item.show();
                }
            });
        }
    },


    showDevicegroupDetailsView: function (currentDeviceGroupId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            model = me.getModel('Mdc.model.DeviceGroup'),
            widget;
        if (this.getApplication().getController('Mdc.controller.setup.AddDeviceGroupAction').router) {
            this.getApplication().getController('Mdc.controller.setup.AddDeviceGroupAction').router = null;
        }
        this.getDevicesOfDeviceGroupStore().getProxy().setExtraParam('id', currentDeviceGroupId);
        this.getDevicesOfDeviceGroupStore().on('sort', function () {
            me.getDevicesOfDeviceGroupGrid().setLoading(false);
        });
        this.getDevicesOfDeviceGroupStore().on('beforesort', function () {
            me.getDevicesOfDeviceGroupGrid().setLoading(true);
        });

        var service = Ext.create('Mdc.service.Search', {
            router: router
        });
        var domainsStore = service.getSearchDomainsStore();
        domainsStore.load(function () {
            service.applyState({
                domain: 'com.energyict.mdc.device.data.Device',
                filters: [{
                    property: 'deviceGroup',
                    value: [{
                        criteria: currentDeviceGroupId,
                        operator: '=='
                    }]
                }]
            });
        });

        widget = Ext.widget('device-groups-details', {
            router: router,
            deviceGroupId: currentDeviceGroupId,
            service: service
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        model.load(currentDeviceGroupId, {
            success: function (record) {
                Ext.suspendLayouts();
                widget.down('#devicegroups-view-menu').setHeader(record.get('name'));
                widget.down('form').loadRecord(record);
                Ext.resumeLayouts(true);
                widget.down('device-group-action-menu').record = record;
                me.getApplication().fireEvent('loadDeviceGroup', record);
                me.updateCriteria(record);
                me.updateActionMenuVisibility(record);
            }
        });
    },

    getDeviceCount: function () {
        var me = this;
        me.fireEvent('loadingcount');
        Ext.Ajax.suspendEvent('requestexception');
        me.getCountButton().up('panel').setLoading(true);
        Ext.Ajax.request({
            url: this.getDevicesOfDeviceGroupStore().getProxy().url.replace('{id}', this.getDevicesOfDeviceGroupStore().getProxy().extraParams.id) + '/count',
            timeout: 120000,
            method: 'GET',
            success: function (response) {
                me.getCountButton().setText(JSON.parse(response.responseText).numberOfSearchResults);
                me.getCountButton().setDisabled(true);
                me.getCountButton().up('panel').setLoading(false);
            },
            failure: function (response, request) {
                var box = Ext.create('Ext.window.MessageBox', {
                    buttons: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.close', 'MDC', 'Close'),
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
                                me.getCountButton().setDisabled(true);
                                me.getCountButton().up('panel').setLoading(false);
                            }
                        }
                    }
                });

                box.show({
                    title: Uni.I18n.translate('general.timeOut', 'MDC', 'Time out'),
                    msg: Uni.I18n.translate('general.timeOutMessageGroups', 'MDC', 'Counting the device group members took too long.'),
                    modal: false,
                    ui: 'message-error',
                    icon: 'icon-warning2',
                    style: 'font-size: 34px;'
                });
            }
        });
    },

    updateActionMenuVisibility: function (record) {
        var actionMenu = this.getDeviceGroupDetailsActionMenu();
        var removeItem = this.getRemoveDeviceGroupMenuItem();
        var editItem = this.getEditDeviceGroupMenuItem();
        if (Mdc.privileges.DeviceGroup.canAdministrateDeviceGroup()) {
            actionMenu.setVisible(true);
            removeItem.setVisible(true);
            editItem.setVisible(true);
        } else if (Mdc.privileges.DeviceGroup.canViewGroupDetails()) {
            actionMenu.setVisible(false);
            removeItem.setVisible(false);
            editItem.setVisible(false);
        } else if (Mdc.privileges.DeviceGroup.canAdministrateDeviceOfEnumeratedGroup()) {
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
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            additionalParams = {},
            route;

        router.arguments.deviceGroupId = menu.record.getId();

        switch (item.action) {
            case 'editDeviceGroup':
                route = 'devices/devicegroups/view/edit';
                break;
            case 'deleteDeviceGroup':
                me.removeDeviceGroup(menu.record);
                break;
        }
        me.getDeviceGroupsGrid() ? additionalParams.fromDetails = false : additionalParams.fromDetails = true;
        route && (route = router.getRoute(route));
        route && route.forward(router.arguments, additionalParams);
    },

    removeDeviceGroup: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show({
            msg: Uni.I18n.translate('deviceGroup.remove.msg', 'MDC', 'This device group will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?", [record.data.name]),
            fn: function (state) {
                if (state === 'confirm') {
                    record.destroy({
                        success: function () {
                            if (me.getPage()) {
                                var grid = me.getPage().down('deviceGroupsGrid');
                                grid.down('pagingtoolbartop').totalCount = 0;
                                grid.down('pagingtoolbarbottom').resetPaging();
                                grid.getStore().load();
                            } else {
                                me.getController('Uni.controller.history.Router').getRoute('devices/devicegroups').forward();
                            }
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceGroup.remove.success.msg', 'MDC', 'Device group removed'));
                        }
                    });
                }
            }
        });
    },

    onGenerateReport: function () {
        var me = this;
        var groupName = '';
        var deviceGroupName = Ext.ComponentQuery.query('#deviceGroupName');

        if ((deviceGroupName != null) && (_.isArray(deviceGroupName))) {
            groupName = deviceGroupName[0].getValue();
        }

        var reportFilter = {};
        reportFilter['search'] = false;
        reportFilter['GROUPNAME'] = groupName;

        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('workspace/generatereport').forward(null, {
            category: 'MDC',
            filter: Ext.JSON.encode(reportFilter)
        });

        return;
    }
});

