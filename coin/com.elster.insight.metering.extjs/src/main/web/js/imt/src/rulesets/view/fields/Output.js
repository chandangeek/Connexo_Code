Ext.define('Imt.rulesets.view.fields.Output', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.output-display',

    renderer: function (value) {
        var icon = {
            style: 'margin-left: 10px;'
        };

        if (value.isMatched) {
            icon.cls = 'icon-checkmark';
            icon.tooltip = Uni.I18n.translate('output.icon.isMatched', 'IMT', "The validation rule set contains rules that have reading types matching the purpose output");
            icon.style += 'color: #41aa4c;';
        } else {
            icon.cls = 'icon-cross';
            icon.tooltip = Uni.I18n.translate('output.icon.isNotMatched', 'IMT', "The validation rule set doesn't contain rules that have reading types matching the purpose output");
        }

        return value.outputName + Ext.String.format('<span class="{0}" style="{1}" data-qtip="{2}"></span>', icon.cls, icon.style, icon.tooltip);
    }
});