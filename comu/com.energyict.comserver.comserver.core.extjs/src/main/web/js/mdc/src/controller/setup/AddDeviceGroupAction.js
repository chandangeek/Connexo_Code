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
        'Uni.view.window.Wizard'
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
        }
    ],

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
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups').forward();
    },

    cancelClick: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/devicegroups').forward();
    },

    showAddDeviceGroupAction: function () {
        widget = Ext.widget('add-devicegroup-browse');
        this.getApplication().fireEvent('changecontentevent', widget);
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
                break;
            case 'deviceGroupWizardStep2' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(true);
                backBtn.setDisabled(false);
                finishBtn.show();
                cancelBtn.show();
                break;
        }
    }


})
