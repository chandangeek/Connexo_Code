Ext.define('Imt.usagepointmanagement.view.forms.fields.DisplayFieldWithIcon', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.displayfieldwithicon',
    iconsMap: {
        "ELECTRICITY": '<span class="icon-power"></span>',
        "GAS": '<span class="icon-fire2"></span>',
        "WATER": '<span class="icon-droplet"></span>',
        "THERMAL": '<span class="icon-rating3"></span>',
        "CONNECTED": '<span class="icon-link"></span>',
        "PHYSICALLYDISCONNECTED": '<span class="icon-link2"></span>',
        "LOGICALDISCONNECTED": '<span class="icon-link5"></span>',
        "UNKNOWN": '<span class="icon-blocked"></span>'
    },

    renderer: function (value) {
        var icon = this.iconsMap[value];
        return value + "&nbsp" + icon;
    }
});