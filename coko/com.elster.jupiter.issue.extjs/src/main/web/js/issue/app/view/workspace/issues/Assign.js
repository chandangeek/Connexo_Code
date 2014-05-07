Ext.define('Isu.view.workspace.issues.Assign', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.Hidden'
    ],
    alias: 'widget.issues-assign',

    content: {
        items: {
            xtype: 'issues-assign-form'
        },
        buttons: [
            {
                text: 'Assign',
                name: 'assign',
                formBind: false
            },
            {
                text: 'Cancel',
                name: 'cancel',
                ui: 'link',
                hrefTarget: '',
                href: '#/workspace/datacollection/issues'
            }
        ]
    }
});