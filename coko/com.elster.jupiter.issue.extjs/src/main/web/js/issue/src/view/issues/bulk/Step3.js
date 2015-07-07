Ext.define('Isu.view.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: 'Action details',

    requires: [
        'Isu.view.issues.CloseForm',
        'Isu.view.issues.AssignForm'
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});