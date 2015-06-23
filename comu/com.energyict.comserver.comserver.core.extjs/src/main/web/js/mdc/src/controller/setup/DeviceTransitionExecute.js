Ext.define('Mdc.controller.setup.DeviceTransitionExecute', {
    extend: 'Ext.app.Controller',

//    requires: [
//        'Mdc.model.DeviceTransition'
//    ],

    views: [
        'Mdc.view.setup.devicetransitionexecute.Browse'
    ],


    models: [
        'Mdc.model.DeviceTransition'
    ],

    refs: [
        {ref: 'navigationMenu', selector: '#deviceTransitionWizardNavigation'},
        {ref: 'deviceTransitionExecuteWizard', selector: '#deviceTransitionExecuteWizard'},
        {ref: 'nextBtn', selector: 'deviceTransitionExecuteWizard #nextButton'},
        {ref: 'cancelBtn', selector: 'deviceTransitionExecuteWizard #wizardCancelButton'},
        {ref: 'step2', selector: '#devicetransitionexecute-wizard-step2'}
    ],

    init: function () {
        this.control({
            'deviceTransitionExecuteWizard #nextButton': {
                click: this.nextClick
            },
            'deviceTransitionExecuteWizard #wizardCancelButton': {
                click: this.cancelClick
            }
        });
    },

    cancelClick: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('devices/device').forward();
    },

    nextClick: function () {
        var me = this,
            layout = me.getDeviceTransitionExecuteWizard().getLayout(),
            propertyForm = me.getDeviceTransitionExecuteWizard().down('property-form'),
            router = me.getController('Uni.controller.history.Router'),
            step2Page = me.getStep2(),
            record;

        me.getNavigationMenu().moveNextStep();
        me.hideWizardBtns();
        layout.setActiveItem(layout.getNext());
        step2Page.showProgressBar();

        propertyForm.updateRecord();
        record = propertyForm.getRecord();

        record.save({
            success: function (record, operation) {
                step2Page.handleSuccessRequest(Ext.decode(operation.response.responseText), router);
            },
            failure: function (record, resp) {
                Ext.suspendLayouts();
                layout.setActiveItem(layout.getPrev());
                me.showWizardBtns();
                Ext.resumeLayouts(true);
            }
        });
    },

    hideWizardBtns: function () {
        this.getNextBtn().hide();
        this.getCancelBtn().hide();
    },

    showWizardBtns: function () {
        this.getNextBtn().show();
        this.getCancelBtn().show();
    },

    showExecuteTransition: function (mRID, transition) {
        var me = this,
            widget = Ext.widget('deviceTransitionExecuteBrowse'),
            transitionModel = me.getModel('Mdc.model.DeviceTransition');

        transitionModel.getProxy().setUrl(mRID);

        transitionModel.load(transition, {
            success: function (record) {
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('property-form').loadRecord(record);
            },
            callback: function () {
                console.log('callback')
            }
        });

    }

});

