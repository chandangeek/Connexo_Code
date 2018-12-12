/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.ThreeValuesDisplayField', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.threevaluesdisplayfield',
    renderer: function (value) {
        switch (value) {
            case "YES":
                return Uni.I18n.translate('general.label.yes', 'IMT', 'Yes');
                break;
            case "NO":
                return Uni.I18n.translate('general.label.no', 'IMT', 'No');
                break;
            default:
                return Uni.I18n.translate('general.label.unknown', 'IMT', 'Unknown');
                break;
        }
    }
});