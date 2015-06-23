Ext.define('Mdc.view.setup.devicetransitionexecute.Step2', {
    extend: 'Ext.panel.Panel',
    xtype: 'devicetransitionexecute-wizard-step2',
    name: 'devicetransitionWizardStep2',
    ui: 'large',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    title: Uni.I18n.translate('devicetransitionexecute.wizard.step2title', 'MDC', 'Step 2 of 2:  Status'),

    showProgressBar: function() {
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

    handleSuccessRequest: function(response, router) {
        var messagePanel;
        if (response.result) {
            var msgParams = {
                type: 'success',
                msgBody: [
                    {html: '1'}
                ],
                closeBtn: false
            };
        } else {
            var msgParams = {
                type: 'error',
                msgBody: [
                    {html: '2'}
                ],
                btns: [
                    {text: "Finish", hnd: function () {
                        router.getRoute('devices/device').forward();
                    }}
                ],
                closeBtn: false
            };
        }
        Ext.suspendLayouts();
        messagePanel = Ext.widget('message-panel', msgParams);
        this.removeAll(true);
        this.add(messagePanel);
        Ext.resumeLayouts(true);
    }
});
