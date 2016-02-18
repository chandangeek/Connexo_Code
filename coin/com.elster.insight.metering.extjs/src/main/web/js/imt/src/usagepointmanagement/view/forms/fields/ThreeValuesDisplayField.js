Ext.define('Imt.usagepointmanagement.view.forms.fields.ThreeValuesDisplayField', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.threevaluesdisplayfield',
    renderer: function (value) {
        switch (value) {
            case true:
                return Uni.I18n.translate('general.label.yes', 'IMT', 'Yes');
                break;
            case false:
                return Uni.I18n.translate('general.label.no', 'IMT', 'No');
                break;
            default:
                return Uni.I18n.translate('general.label.unknown', 'IMT', 'Unknown');
                break;
        }
    }
});