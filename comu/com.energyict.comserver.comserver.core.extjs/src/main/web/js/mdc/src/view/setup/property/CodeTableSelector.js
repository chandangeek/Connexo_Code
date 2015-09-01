Ext.define('Mdc.view.setup.property.CodeTableSelector', {
    extend: 'Ext.form.field.Picker',
    alias: 'widget.codeTableSelector',
    alternateClassName: ['codeTableSelector'],

    createPicker: function () {
        var me = this,
            picker = new Ext.panel.Panel({
                pickerField: me,
                floating: true,
                hidden: true,
                ownerCt: this.ownerCt,
                renderTo: document.body,
                title: Uni.I18n.translate('property.codeTable','MDC','CodeTable'),
                height: 200,
                html: ""
            });

        return picker;
    }

});