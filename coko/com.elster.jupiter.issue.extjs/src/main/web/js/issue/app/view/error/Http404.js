Ext.define('Mtr.view.error.Http404', {
    extend: 'Mtr.view.error.Page',
    alias: 'widget.errorHttp404',
    requires: [
        'Mtr.view.error.Page'
    ],

    initComponent: function () {
        this.callParent(arguments);

        // TODO Fix this so it doesn't throw an invalid reference error.
//        this.setErrorTitle('<h1>Page not found</h1>');
//        this.setErrorMessage('<p style="margin: 10px;">The requested page could not be found.</p>');
    }
});