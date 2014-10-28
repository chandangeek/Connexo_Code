Ext.define('Idc.view.workspace.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: 'Action details',

    requires: [
        'Idc.view.workspace.issues.CloseForm',
        'Idc.view.workspace.issues.AssignForm'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});