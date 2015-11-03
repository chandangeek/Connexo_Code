Ext.define('Sam.controller.about.About', {
    extend: 'Ext.app.Controller',

    stores: [
        'Sam.store.AvailableAndLicensedApplications'
    ],

    views: [
        'Sam.view.about.About'
    ],

    showAbout: function () {
        var me = this;

        me.getStore('Sam.store.AvailableAndLicensedApplications').load();
        me.getApplication().fireEvent('changecontentevent', Ext.widget('about-info'), {itemId: 'about-info-page'});
    }
});