Ext.define('Idv.view.workspace.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: 'Action details',

    requires: [
        'Idv.view.workspace.issues.CloseForm',
        'Idv.view.workspace.issues.AssignForm'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});