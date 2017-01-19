Ext.define('Imt.usagepointmanagement.view.transitionexecute.Step2', {
    extend: 'Ext.panel.Panel',
    xtype: 'usagepointtransitionexecute-wizard-step2',
    ui: 'large',
    requires: [
        'Imt.usagepointmanagement.view.transitionexecute.ResultPanel'
    ],

    title: Uni.I18n.translate('usagepointtransitionexecute.wizard.step2.title', 'IMT', 'Step 2: Status'),

    showProgressBar: function () {
        var me = this,
            pb = Ext.create('Ext.ProgressBar', {width: '50%'});

        Ext.suspendLayouts();
        me.removeAll(true);
        me.add(
            pb.wait({
                interval: 50,
                increment: 20,
                text: Uni.I18n.translate('usagepointtransitionexecute.wizard.progressbar', 'IMT', 'Performing pre-transitions checks and auto actions. Please wait...')
            })
        );
        Ext.resumeLayouts(true);
    },

    handleSuccessRequest: function (response, router) {
        var me = this;

        Ext.suspendLayouts();
        me.removeAll(true);        
        me.add({
                xtype: 'transition-result-panel',
                itemId: 'transition-result-panel',
                response: response,
                router: router                
            }
        );
        Ext.resumeLayouts(true);
    }
});
