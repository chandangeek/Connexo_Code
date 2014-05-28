Ext.define('Isu.view.workspace.issues.IssueNoGroup', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-no-group',
    itemId: 'IssueNoGroup',
    hidden: true,

    items: [
        {
            itemId : 'NoGroup_text',
            html: '<h3>No group selected</h3><p>Select a group of issues.</p>',
            bodyPadding: 10,
            border: false
        }
    ]
});