Ext.define('Imt.usagepointmanagement.view.forms.fields.DisplayFieldWithIcon', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.displayfieldwithicon',
    iconsMap: {
        "ELECTRICITY": Uni.I18n.translate('general.attributes.usagePoint.electricity', 'IMT', 'Electricity <span class="icon-power"></span>'),
        "GAS": Uni.I18n.translate('general.attributes.usagePoint.gas', 'IMT', 'Gas <span class="icon-fire2"></span>'),
        "WATER": Uni.I18n.translate('general.attributes.usagePoint.water', 'IMT', 'Water <span class="icon-droplet"></span>'),
        "HEAT": Uni.I18n.translate('general.attributes.usagePoint.thermal', 'IMT', 'Thermal <span class="icon-rating3"></span>'),
        "CONNECTED":  Uni.I18n.translate('general.attributes.usagePoint.connected', 'IMT', 'Connected <span class="icon-rating3"></span>'),
        "PHYSICALLYDISCONNECTED":  Uni.I18n.translate('general.attributes.usagePoint.physicallyDisconnected', 'IMT', 'Physically disconnected <span class="icon-rating3"></span>'),
        "LOGICALLYDISCONNECTED":  Uni.I18n.translate('general.attributes.usagePoint.logicallyDisconnected', 'IMT', 'Lgically disconnected <span class="icon-rating3"></span>'),
        "UNKNOWN":  Uni.I18n.translate('general.attributes.usagePoint.unknown', 'IMT', 'Unknown <span class="icon-blocked"></span>')
    },

    renderer: function (value) {
        return this.iconsMap[value];
    }
});