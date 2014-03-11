Ext.define('Isu.view.workspace.issues.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-overview',
    title: 'Issues overview',

    requires: [
        'Isu.view.workspace.issues.Browse',
        'Isu.view.workspace.issues.SideFilter'
    ],

    content: [
        {
            xtype: 'issues-browse'
        }
    ],

    side: [
        {
            xtype: 'issues-side-filter'
        }
    ]
});