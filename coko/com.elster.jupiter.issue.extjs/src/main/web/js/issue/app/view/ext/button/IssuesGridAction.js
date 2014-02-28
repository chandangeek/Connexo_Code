Ext.define('Mtr.view.ext.button.IssuesGridAction', {
    extend: 'Ext.button.Split',
    cls: 'isu-grid-action-btn',
    menuAlign: 'tl-bl',
    menu: {
        xtype: 'menu',
        name: 'issueactionmenu',
        shadow: false,
        border: false,
        plain: true,
        cls: 'issue-action-menu',
        items: [
            {
                text: 'Assign'
            },
            {
                text: 'Close'
            }
        ]
    }
});