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
        {ref: 'deviceGroupsGrid', selector: '#deviceGroupsGrid'},
        {ref: 'deviceGroupPreviewForm', selector: '#deviceGroupPreviewForm'},
        {ref: 'deviceGroupPreview', selector: '#deviceGroupPreview'},
        {ref: 'searchCriteriaContainer', selector: '#searchCriteriaContainer'},
        {ref: 'createDeviceGroupButton', selector: '#createDeviceGroupButton'},
        {ref: 'devicesOfDeviceGroupGrid', selector: '#allDevicesOfDeviceGroupGrid'},
        {ref: 'devicesOfDeviceGroupGrid', selector: '#allDevicesOfDeviceGroupGrid'}

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
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    previewDeviceGroup: function (grid, record) {
        var deviceGroups = this.getDeviceGroupsGrid().getSelectionModel().getSelection();
        if (deviceGroups.length == 1) {
            var deviceGroup = deviceGroups[0];
            this.getDeviceGroupPreviewForm().loadRecord(deviceGroup);
            this.getDeviceGroupPreview().setTitle(deviceGroup.get('name'));


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
        var me = this;
        if (record.get('dynamic')) {
            me.getSearchCriteriaContainer().setVisible(true);
            var criteria = record.criteriaStore.data.items;
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
                            return criteriaValue;
                        }
                    }
                )
            }
        } else {
            me.getSearchCriteriaContainer().setVisible(false);
        }
    },



    showDevicegroupDetailsView: function (currentDeviceGroupId) {
        var me = this;
        var router = me.getController('Uni.controller.history.Router');

        this.getDevicesOfDeviceGroupStore().getProxy().setExtraParam('id', currentDeviceGroupId);
        this.getDevicesOfDeviceGroupStore().load({
            callback: function () {
                var widget = Ext.widget('device-groups-details', {
                    router: router,
                    deviceGroupId: currentDeviceGroupId
                });
                var actionsMenu = widget.down('device-group-action-menu');

                var model = Ext.ModelManager.getModel('Mdc.model.DeviceGroup');
                model.load(currentDeviceGroupId, {
                    success: function (record) {
                        actionsMenu.record = record;
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.down('form').loadRecord(record);
                        me.getApplication().fireEvent('loadDeviceGroup', record);
                        me.updateCriteria(record);
                    }
                });
            }
        });

    }
});

