Ext.define('Mdc.controller.setup.DevicesAddGroupController', {
    extend: 'Ext.app.Controller',

    prefix: 'add-devicegroup-browse',

    requires: [
        'Mdc.view.setup.devicesearch.SearchItems',
        'Uni.form.filter.FilterCombobox',
        'Mdc.view.setup.devicegroup.Step2',
        'Mdc.view.setup.devicegroup.Browse',
        'Mdc.view.setup.devicegroup.Edit'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.DeviceFilter'
    ],

    stores: [
        'Mdc.store.Devices',
        'Mdc.store.filter.DeviceTypes',
        'Mdc.store.DevicesBuffered'
    ],

    refs: [
        {
            ref: 'dynamicRadioButton',
            selector: 'devicegroup-wizard-step1 #dynamicDeviceGroup'
        },
        {
            ref: 'dynamicGridFilter',
            selector: 'devicegroup-wizard-step2 mdc-view-setup-devicesearch-devicestopfilter'
        },
        {
            ref: 'staticGridFilter',
            selector: 'devicegroup-wizard-step2 mdc-view-setup-devicesearch-buffereddevicestopfilter'
        },
        {
            ref: 'editPage',
            selector: 'device-group-edit'
        }
    ],

    applyFilters: function () {
        this.initAddDeviceGroupActionController();
        if ((this.getDynamicRadioButton() && this.getDynamicRadioButton().checked) || (!this.getDynamicRadioButton() && this.isDynamic)) {
            this.getDynamicGridFilter().applyFilters();
        } else {
            this.getStaticGridFilter().applyFilters();
        }
    },

    initAddDeviceGroupActionController: function () {
        if (this.getEditPage()) {
            this.isDynamic = this.getApplication().getController('Mdc.controller.setup.AddDeviceGroupAction').dynamic;
        } else {
            this.getApplication().getController('Mdc.controller.setup.AddDeviceGroupAction').disableCreateWidget();
        }
    },

});