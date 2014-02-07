Ext.define('Mdc.controller.setup.RegisterMappings', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.register.RegisterMappingsSetup',
        'setup.register.RegisterMappingsGrid',
        'setup.register.RegisterMappingPreview'
    ],

    requires: [
        'Mdc.store.RegisterMappings'
    ],

    stores: [
        'RegisterMappings'
    ],

    refs: [
        {ref: 'registerMappingGrid', selector: '#registermappinggrid'},
        {ref: 'registerMappingPreviewForm', selector: '#registerMappingPreviewForm'},
        {ref: 'registerMappingPreview', selector: '#registerMappingPreview'},
        {ref: 'registerMappingPreviewTitle', selector: '#registerMappingPreviewTitle'}
    ],

    init: function () {

        this.control({
            '#registermappinggrid': {
                selectionchange: this.previewRegisterMapping
            }
        });
    },

    showEditView: function (id) {

    },

    previewRegisterMapping: function (grid, record) {
        console.log('preview register mapping');
         var registerMappings = this.getRegisterMappingGrid().getSelectionModel().getSelection();
         if (registerMappings.length == 1) {
         this.getRegisterMappingPreviewForm().loadRecord(registerMappings[0]);
         var registerMappingsName = this.getRegisterMappingPreviewForm().form.findField('name').getSubmitValue();
         this.getRegisterMappingPreview().show();
         this.getRegisterMappingPreviewTitle().update('<h4>' + registerMappingsName + '</h4>');
         } else {
         this.getRegisterMappingPreview().hide();
         }
    },

    showRegisterMappings: function (id) {
        this.getRegisterMappingsStore().getProxy().setExtraParam('deviceType', id);
        var widget = Ext.widget('registerMappingsSetup');
        Mdc.getApplication().getMainController().showContent(widget);
    }

})
;
