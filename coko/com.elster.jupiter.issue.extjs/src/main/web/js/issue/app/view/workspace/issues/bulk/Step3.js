Ext.define('Isu.view.workspace.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: 'Action details',

    requires: [
        'Isu.view.workspace.issues.CloseForm',
        'Isu.view.workspace.issues.AssignForm'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});