Ext.define('Mdc.controller.setup.AddDeviceGroupAction', {
    extend: 'Ext.app.Controller',
    views: [
        'Mdc.view.setup.devicegroup.Step1',
        'Mdc.view.setup.devicegroup.Step2',
        'Mdc.view.setup.devicegroup.Browse',
        'Mdc.view.setup.devicegroup.Navigation',
        'Mdc.view.setup.devicegroup.Wizard'
    ],
    requires: [
        'Uni.view.window.Wizard',
        'Mdc.view.setup.devicesearch.DevicesSideFilter'
    ],

    refs: [
        {
            ref: 'backButton',
            selector: 'adddevicegroup-wizard #backButton'
        },
        {
            ref: 'nextButton',
            selector: 'adddevicegroup-wizard #nextButton'
        },
        {
            ref: 'confirmButton',
            selector: 'adddevicegroup-wizard #confirmButton'
        },
        {
            ref: 'finishButton',
            selector: 'adddevicegroup-wizard #finishButton'
        },
        {
            ref: 'wizardCancelButton',
            selector: 'adddevicegroup-wizard #wizardCancelButton'
        },
        {
            ref: 'navigationMenu',
            selector: '#devicegroupaddnavigation'
        },
        {
            ref: 'addDeviceGroupWizard',
            selector: '#adddevicegroupwizard'
        },
        {
            ref: 'addDeviceGroupSideFilter',
            selector: 'add-devicegroup-browse #addDeviceGroupSideFilter'
        },
        {
            ref: 'nameTextField',
            selector: 'devicegroup-wizard-step1 #deviceGroupNameTextField'
        },
        {
            ref: 'dynamicRadioButton',
            selector: 'devicegroup-wizard-step1 #dynamicDeviceGroup'
        },
        {
            ref: 'filterForm',
            selector: '#addDeviceGroupSideFilter form'
        }
    ],

    addDeviceGroupWidget: null,

    init: function () {
        this.control({
            'adddevicegroup-wizard #backButton': {
                click: this.backClick
            },
            'adddevicegroup-wizard #nextButton': {
                click: this.nextClick
            },
            'adddevicegroup-wizard #confirmButton': {
                click: this.confirmClick
            },
            'adddevicegroup-wizard #finishButton': {
                click: this.finishClick
            },
            'adddevicegroup-wizard #wizardCancelButton': {
                click: this.cancelClick
            }
        });
    },

    backClick: function () {
        var layout = this.getAddDeviceGroupWizard().getLayout(),
            currentCmp = layout.getActiveItem();
        this.getNavigationMenu().movePrevStep();
        this.changeContent(layout.getPrev(), currentCmp);
    },

    nextClick: function () {
        var layout = this.getAddDeviceGroupWizard().getLayout();
        this.getNavigationMenu().moveNextStep();
        this.changeContent(layout.getNext(), layout.getActiveItem());
    },

    confirmClick: function () {

    },

    finishClick: function () {
        this.addDeviceGroupWidget = null;
        this.addDeviceGroup();
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups').forward();
    },

    addDeviceGroup: function () {
        var me = this;
        var record = Ext.create('Mdc.model.DeviceGroup');
        var router = this.getController('Uni.controller.history.Router');
        var preloader = Ext.create('Ext.LoadMask', {
            msg: "Loading...",
            target: this.getAddDeviceGroupWizard()
        });
        preloader.show();
        if (record) {
            record.set('name', this.getNameTextField().getValue());
            record.set('dynamic', this.getDynamicRadioButton().checked);
            record.set('filter', this.getController('Uni.controller.history.Router').filter.data);
            record.save({
                /*Ext.Ajax.request({
                 url: '../../api/ddr/devicegroups',
                 method: 'POST',
                 jsonData: {
                 mRID: this.getNameTextField().getValue(),
                 dynamic: this.getDynamicRadioButton().checked
                 },*/
                success: function () {
                    me.getController('Uni.controller.history.Router').getRoute('devices/devicegroups').forward();
                    me.getApplication().fireEvent('acknowledge', 'Device group added');
                },
                failure: function (response) {
                    if (response.status == 400) {
                        var result = Ext.decode(response.responseText, true),
                            errorTitle = 'Failed to add',
                            errorText = 'Device group could not be added. There was a problem accessing the database';

                        if (result !== null) {
                            errorTitle = result.error;
                            errorText = result.message;
                        }
                        self.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                    }
                },
                callback: function () {
                    preloader.destroy();
                }
            });
        }
    },

    cancelClick: function () {
        this.addDeviceGroupWidget = null;
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups').forward();
    },

    showAddDeviceGroupAction: function () {
        if (this.addDeviceGroupWidget == null) {
            this.addDeviceGroupWidget = Ext.widget('add-devicegroup-browse');
            this.getAddDeviceGroupSideFilter().setVisible(false);
            this.getApplication().fireEvent('changecontentevent', this.addDeviceGroupWidget);
        }
    },


    changeContent: function (nextCmp, currentCmp) {
        var layout = this.getAddDeviceGroupWizard().getLayout();
        layout.setActiveItem(nextCmp);
        this.updateButtonsState(nextCmp);
    },

    updateButtonsState: function (activePage) {
        var me = this,
            wizard = me.getAddDeviceGroupWizard(),
            backBtn = wizard.down('#backButton'),
            nextBtn = wizard.down('#nextButton'),
            finishBtn = wizard.down('#finishButton'),
            cancelBtn = wizard.down('#wizardCancelButton');
        switch (activePage.name) {
            case 'deviceGroupWizardStep1' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(false);
                backBtn.setDisabled(true);
                finishBtn.hide();
                cancelBtn.show();
                this.getAddDeviceGroupSideFilter().setVisible(false);
                break;
            case 'deviceGroupWizardStep2' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(true);
                backBtn.setDisabled(false);
                finishBtn.show();
                cancelBtn.show();
                this.getAddDeviceGroupSideFilter().setVisible(true);
                break;
        }
    }


})
