Ext.define('Mdc.view.setup.devicetransitionexecute.Step2', {
    extend: 'Ext.panel.Panel',
    xtype: 'devicetransitionexecute-wizard-step2',
    name: 'devicetransitionWizardStep2',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Mdc.view.setup.devicetransitionexecute.ResultPanel'
    ],

    title: Uni.I18n.translate('devicetransitionexecute.wizard.step2title', 'MDC', 'Step 2 of 2:  Status'),

    showProgressBar: function () {
        var me = this,
            pb = Ext.create('Ext.ProgressBar', {width: '50%'});

        Ext.suspendLayouts();
        me.removeAll(true);
        me.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: Uni.I18n.translate('devicetransitionexecute.wizard.progressbar', 'MDC', 'Performing configured pre-transition checks and auto changes. Please wait...')
            })
        );
        Ext.resumeLayouts(true);
    },

    handleSuccessRequest: function (response, router, deviceRemoved) {
        var me = this;

        Ext.suspendLayouts(true);
        me.removeAll(true);
        me.add({
                xtype: 'transition-result-panel',
                response: response,
                router: router,
                deviceRemoved: deviceRemoved
            }
        );
        Ext.resumeLayouts(true);
    }
});
