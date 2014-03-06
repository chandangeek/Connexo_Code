Ext.define('Mtr.view.workspace.issues.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step3',
    title: 'Action details',
    bodyCls: 'isu-bulk-wizard-no-border',

    requires: [
        'Mtr.view.workspace.issues.Close',
        'Mtr.view.workspace.issues.AssignForm'
    ],

    listeners: {
        removechildborder: function (panel) {
            panel.items.items[0].addBodyCls('isu-bulk-wizard-no-border');
        }
    },

    initComponent: function () {
        this.callParent(arguments);
    }
});