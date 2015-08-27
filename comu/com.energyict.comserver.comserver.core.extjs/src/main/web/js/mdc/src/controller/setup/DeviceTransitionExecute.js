Ext.define('Mdc.controller.setup.DeviceTransitionExecute', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.devicetransitionexecute.Browse'
    ],

    models: [
        'Mdc.model.Device',
        'Mdc.model.DeviceTransition'
    ],

    refs: [
        {ref: 'navigationMenu', selector: '#deviceTransitionWizardNavigation'},
        {ref: 'deviceTransitionExecuteWizard', selector: '#deviceTransitionExecuteWizard'},
        {ref: 'nextBtn', selector: 'deviceTransitionExecuteWizard #nextButton'},
        {ref: 'cancelBtn', selector: 'deviceTransitionExecuteWizard #wizardCancelButton'},
        {ref: 'step2', selector: '#devicetransitionexecute-wizard-step2'},
        {ref: 'transitionDateField', selector: '#transitionDateField'}
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
            transitionDateField = me.getTransitionDateField(),
            transitionFieldValue = transitionDateField.getValue(),
            record;

        me.getNavigationMenu().moveNextStep();
        me.hideWizardBtns();
        layout.setActiveItem(layout.getNext());
        step2Page.showProgressBar();

        propertyForm.updateRecord();
        record = propertyForm.getRecord();
        if (!transitionFieldValue.transitionNow) {
            record.set('effectiveTimestamp', transitionFieldValue.time);
        } else {
            record.set('effectiveTimestamp', null);
        }
        var deviceRemoved = record.get('name')==='Remove';
        record.save({
            success: function (record, operation) {
                step2Page.handleSuccessRequest(Ext.decode(operation.response.responseText), router, deviceRemoved);
            },
            failure: function () {
                Ext.suspendLayouts();
                layout.setActiveItem(layout.getPrev());
                me.showWizardBtns();
                me.getNavigationMenu().movePrevStep();
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

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                me.getApplication().fireEvent('loadDevice', device);
            }
        });

        transitionModel.getProxy().setUrl(mRID);
        me.getApplication().fireEvent('changecontentevent', widget);

        widget.setLoading();
        transitionModel.load(transition, {
            success: function (record) {
                widget.down('property-form').loadRecord(record);
                me.getApplication().fireEvent('loadDeviceTransition', record);
            },
            callback: function () {
                widget.setLoading(false);
            }
        });
    }
});

