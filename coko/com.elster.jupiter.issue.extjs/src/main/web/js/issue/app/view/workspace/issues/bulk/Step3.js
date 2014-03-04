Ext.define('Mtr.view.workspace.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: 'Action details',

    requires: [
        'Mtr.view.workspace.issues.Close',
        'Mtr.view.workspace.issues.AssignForm'
    ],

    items: [],

    initComponent: function () {
        this.callParent(arguments);
    }
});