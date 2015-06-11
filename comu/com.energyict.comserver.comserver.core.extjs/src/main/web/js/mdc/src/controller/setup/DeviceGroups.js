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

    mixins: [

    ],

    refs: [
        {ref: 'page', selector: 'deviceGroupSetup'},
        {ref: 'deviceGroupsGrid', selector: '#deviceGroupsGrid'},
        {ref: 'deviceGroupPreviewForm', selector: '#deviceGroupPreviewForm'},
        {ref: 'deviceGroupPreview', selector: '#deviceGroupPreview'},
        {ref: 'searchCriteriaContainer', selector: '#searchCriteriaContainer'},
        {ref: 'createDeviceGroupButton', selector: '#createDeviceGroupButton'},
        {ref: 'devicesOfDeviceGroupGrid', selector: '#allDevicesOfDeviceGroupGrid'},
        {ref: 'devicesOfDeviceGroupGrid', selector: '#allDevicesOfDeviceGroupGrid'},
        {ref: 'deviceGroupDetailsActionMenu', selector: '#deviceGroupDetailsActionMenu'},
        {ref: 'removeDeviceGroupMenuItem', selector: '#remove-device-group'},
        {ref: 'editDeviceGroupMenuItem', selector: '#edit-device-group'}

        /*{
            ref: 'deviceGroupFormForDetails',
            selector: 'deviceGroupDetailsForm fieldcontainer'
        },
        {
            ref: 'deviceGroupFormForPreview',
            selector: 'deviceGroupPreviewForm fieldcontainer'
        }*/

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
            }

            /*,
             '#deviceGroupsGrid actioncolumn': {
             deleteDeviceGroup: this.deleteDeviceGroup
             },
             '#deviceGroupPreview menuitem[action=deleteDeviceGroup]': {
             click: this.deleteDeviceGroupFromPreview
             }    */
        });
    },

    showAddDeviceGroupWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups/add').forward();
    },

    /*deleteDeviceGroup: function (deviceGroupToDelete) {
     var me = this;
     var msg = msg = Uni.I18n.translate('deviceGroup.deleteDeviceGroup', 'MDC', 'The device group will no longer be available.');

     Ext.create('Uni.view.window.Confirmation').show({
     msg: msg,
     title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' ' + deviceGroupToDelete.get('name') + '?',
     config: {
     deviceGroupToDelete: deviceGroupToDelete,
     me: me
     },
     fn: me.removeDeviceGroup
     });
     },

     removeDeviceGroup: function (btn, text, opt) {
     if (btn === 'confirm') {
     var deviceGroupToDelete = opt.config.deviceGroupToDelete;
     var me = opt.config.me;

     deviceGroupToDelete.destroy({
     success: function () {
     me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceGroup.acknowlegment.removed', 'MDC', 'Device group removed') );
     location.href = '#/devices/devicegroups/';
     }
     });

     }
     },

     deleteDeviceGroupFromPreview: function () {
     this.deleteDeviceGroup(this.getDeviceGroupsGrid().getSelectionModel().getSelection()[0]);
     },  */

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
            var deviceGroup = deviceGroups[0];
            this.getDeviceGroupPreviewForm().loadRecord(deviceGroup);
            this.getDeviceGroupPreview().setTitle(Ext.String.htmlEncode(deviceGroup.get('name')));
            this.getDeviceGroupPreview().down('device-group-action-menu').record = deviceGroup;
            this.updateCriteria(deviceGroup);
        }
    },

    translateCriteriaName: function(criteriaName) {
        if (criteriaName == 'deviceConfiguration.deviceType.name') {
            criteriaName = Uni.I18n.translate('devicetype.deviceType', 'MDC', 'Device type')
        }
        else if (criteriaName == 'mRID') {
            criteriaName = Uni.I18n.translate('deviceGeneralInformation.mrid', 'MDC', 'MRID')
        }
        else if (criteriaName == 'serialNumber') {
            criteriaName = Uni.I18n.translate('deviceGeneralInformation.serialNumber', 'MDC', 'Serial number')
        }
        else if (criteriaName == 'deviceConfiguration.name') {
            criteriaName = Uni.I18n.translate('deviceGeneralInformation.deviceConfiguration', 'MDC', 'Device configuration')
        }
        return criteriaName;
    },

    back: function () {
        location.href = "#devices";
    },

    updateCriteria: function(record) {
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
        if (record.get('dynamic')) {
            me.getSearchCriteriaContainer().setVisible(true);
            var criteria = record.criteriaStore.data.items;
            //Ext.suspendLayouts();
            me.getSearchCriteriaContainer().removeAll();
            for (var i = 0; i < criteria.length; i++) {
                var foundCriteria = criteria[i].data;
                var criteriaName = foundCriteria.criteriaName;
                var criteriaValues = foundCriteria.criteriaValues;
                criteriaName = me.translateCriteriaName(criteriaName);
                var criteriaValue = '';
                for (var j = 0; j < criteriaValues.length; j++) {
                    singleCriteriaValue = criteriaValues[j];
                    criteriaValue = criteriaValue + singleCriteriaValue;
                    if (j != (criteriaValues.length - 1)) {
                        criteriaValue = criteriaValue + ', '
                    }
                }
                me.getSearchCriteriaContainer().add(
                    {
                        xtype: 'displayfield',
                        name: 'name',
                        labelWidth: 150,
                        labelAlign: 'left',
                        fieldLabel: criteriaName,
                        renderer: function (value) {
                            return Ext.String.htmlEncode(criteriaValue);
                        }
                    }
                )
            }
            //Ext.resumeLayouts();
        } else {
            Ext.Array.each(Ext.ComponentQuery.query('#edit-device-group'), function (item) {
                if (Mdc.privileges.DeviceGroup.canAdministrateDeviceOfEnumeratedGroup()) { item.show(); }
            });
            me.getSearchCriteriaContainer().setVisible(false);
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
        widget = Ext.widget('device-groups-details', {
            router: router,
            deviceGroupId: currentDeviceGroupId
        });
        me.getApplication().fireEvent('changecontentevent', widget);
        model.load(currentDeviceGroupId, {
            success: function (record) {
                Ext.suspendLayouts();
                widget.down('#devicegroups-view-menu #devicegroups-view-link').setText(record.get('name'));
                widget.down('form').loadRecord(record);
                Ext.resumeLayouts(true);
                widget.down('device-group-action-menu').record = record;
                me.getApplication().fireEvent('loadDeviceGroup', record);
                me.updateCriteria(record);
                me.updateActionMenuVisibility(record);
            }
        });
    },

    updateActionMenuVisibility: function(record) {
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
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' ' + record.data.name + '?',
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
        router.getRoute('generatereport').forward(null, {
            category: 'MDC',
            filter: reportFilter
        });

        return;
    }
});

