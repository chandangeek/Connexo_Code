Ext.define('Mdc.controller.setup.DevicesSearchController', {
    extend: 'Mdc.controller.setup.DevicesController',
    prefix: 'TODO', // TODO

    requires: [
        'Mdc.view.setup.devicesearch.SearchItems'
    ],

    showSearchItems: function () {
        var searchItems = Ext.create('Mdc.view.setup.devicesearch.SearchItems');
        this.getApplication().fireEvent('changecontentevent', searchItems);
    }
});