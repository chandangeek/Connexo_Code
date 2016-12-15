Ext.define('Imt.usagepointmanagement.controller.UsagePointTransitionExecute', {
    extend: 'Ext.app.Controller',

    views: [
        'Imt.usagepointmanagement.view.transitionexecute.Browse'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.usagepointmanagement.model.UsagePointTransition'
    ],

    refs: [
        {ref: 'page', selector: 'usagepointTransitionExecuteBrowse'},
        {ref: 'navigationMenu', selector: '#usagepointTransitionWizardNavigation'},
        {ref: 'usagepointTransitionExecuteWizard', selector: '#usagepointTransitionExecuteWizard'},
        {ref: 'nextBtn', selector: 'usagepointTransitionExecuteWizard #nextButton'},
        {ref: 'cancelBtn', selector: 'usagepointTransitionExecuteWizard #wizardCancelButton'},
        {ref: 'step2', selector: '#usagepointtransitionexecute-wizard-step2'},
        {ref: 'transitionDateField', selector: 'usagepointTransitionExecuteBrowse #transitionDateField'}
    ],

    init: function () {
        this.control({
            'usagepointTransitionExecuteWizard #nextButton': {
                click: this.nextClick
            }
        });
    },

    nextClick: function () {
        var me = this,
            wizard = me.getUsagepointTransitionExecuteWizard(),
            layout = wizard.getLayout(),
            propertyForm = wizard.down('property-form'),
            router = me.getController('Uni.controller.history.Router'),
            record = propertyForm.getRecord();

        Ext.suspendLayouts();
        me.getPage().down('#wizard-buttons').removeAll(true);
        me.getNavigationMenu().moveNextStep();
        layout.setActiveItem(layout.getNext());
        me.getStep2().showProgressBar();
        Ext.resumeLayouts(true);
        propertyForm.updateRecord();
        record.beginEdit();
        record.set('transitionNow', me.getTransitionDateField().down('#installation-time-now').checked);
        record.set('effectiveTimestamp', me.getTransitionDateField().down('#installation-time-at-date-time-field').getValue());
        record.set('usagePoint', _.pick(me.usagePoint.getData(), 'name', 'version'));
        record.endEdit();
        record.save({
            backUrl: router.getRoute('usagepoints/view').buildUrl(),
            callback: function (record, operation) {
                me.getStep2().handleSuccessRequest(Ext.decode(operation.response.responseText), router);
            }
        });
    },

    showExecuteTransition: function (usagepointId, transitionId) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            transitionModel = me.getModel('Imt.usagepointmanagement.model.UsagePointTransition'),
            app = me.getApplication(),
            dependenciesCounter = 2,
            onDependenciesLoad = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    app.fireEvent('changecontentevent', Ext.widget('usagepointTransitionExecuteBrowse', {
                        itemId: 'usagepointTransitionExecuteBrowse',
                        router: me.getController('Uni.controller.history.Router')
                    }));
                    Ext.suspendLayouts();
                    me.getPage().down('usagepointTransitionWizardNavigation').setTitle(me.transition.get('name'));
                    me.getPage().down('property-form').loadRecord(me.transition);
                    Ext.resumeLayouts(true);
                    mainView.setLoading(false);
                }
            };

        mainView.setLoading();
        transitionModel.getProxy().setParams(usagepointId);
        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagepointId, {
            success: function (usagepoint) {
                app.fireEvent('usagePointLoaded', usagepoint);
                me.usagePoint = usagepoint;
                onDependenciesLoad();
            }
        });
        transitionModel.load(transitionId, {
            success: function (record) {
                me.transition = record;
                app.fireEvent('loadUsagePointTransition', record);
                onDependenciesLoad();
            },
            failure: function () {
                onDependenciesLoad();
            }
        });
    }
});
