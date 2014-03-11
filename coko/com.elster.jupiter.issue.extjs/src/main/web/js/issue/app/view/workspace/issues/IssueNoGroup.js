Ext.define('Isu.view.workspace.issues.IssueNoGroup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-no-group',

    hidden: true,

    items: [
        {
            html: '<h3>No group selected</h3><p>Select a group of issues.</p>',
            bodyPadding: 10,
            border: false
        }
    ]
});