/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.DisplayFieldWithIcon', {
    extend: 'Ext.form.field.Display',
    requires: [
        'Imt.util.IconsMap',
        'Imt.util.ServiceCategoryTranslations'
    ],
    alias: 'widget.displayfieldwithicon',
    htmlEncode: false,

    renderer: function (value) {
        var result = '';

        if (Ext.isObject(value) && !Ext.Object.isEmpty(value)) {
            result = value.name
                + ' <span class="'
                + Imt.util.IconsMap.getCls(value.id)
                + '"></span>';
        } else if (Ext.isString(value)) {
            result = Imt.util.ServiceCategoryTranslations.getTranslation(value)
                + ' <span class="'
                + Imt.util.IconsMap.getCls(value)
                + '"></span>';
        }

        return result || '-';
    }
});