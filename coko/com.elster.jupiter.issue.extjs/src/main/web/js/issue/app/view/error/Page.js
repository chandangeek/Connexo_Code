Ext.define('Mtr.view.error.Page', {
    extend: 'Ext.container.Container',
    alias: 'widget.errorPage',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    itemId: 'errorTitle',
                    html: '<h1>An error occurred</h1>'
                },
                {
                    xtype: 'component',
                    itemId: 'errorMessage',
                    html: '<p style="margin: 10px;">An unknown error has occurred. Please contact your system administrator.</p>'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    setErrorTitle: function (title) {
        this.getComponent('errorTitle').update(title);
    },
    setErrorMessage: function (message) {
        this.getComponent('errorMessage').update(message);
    }
});