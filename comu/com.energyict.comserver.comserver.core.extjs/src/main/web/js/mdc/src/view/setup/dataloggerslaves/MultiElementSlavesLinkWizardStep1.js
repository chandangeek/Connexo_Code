Ext.define('Mdc.view.setup.dataloggerslaves.MultiElementSlavesLinkWizardStep1', {
    extend: 'Ext.container.Container',
    alias: 'widget.multi-element-slave-link-wizard-step1',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    requires: [
        'Mdc.view.setup.dataloggerslaves.MultiElementSlaveDeviceAdd'
    ],
    initComponent: function(){
        var me = this;
        me.items = [
            {
                xtype: 'multi-element-slave-device-add'
            }];
        me.callParent(arguments);
    }
});
