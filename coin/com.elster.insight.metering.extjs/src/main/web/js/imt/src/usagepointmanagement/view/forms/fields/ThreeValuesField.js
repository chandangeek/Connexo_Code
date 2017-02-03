/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.ThreeValuesField', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.techinfo-threevaluesfield',
    displayField: 'displayValue',
    valueField: 'value',
    queryMode: 'local',
    forceSelection: true,
    value: 'UNKNOWN',
    store: [
        ['YES', Uni.I18n.translate('general.yes', 'IMT', 'Yes')],
        ['NO', Uni.I18n.translate('general.no', 'IMT', 'No')],
        ['UNKNOWN', Uni.I18n.translate('general.unknown', 'IMT', 'Unknown')]
    ]
});