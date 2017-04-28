/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.rulesets.view.fields.State', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.state-display',

    renderer: function (value) {
        if(value.name){
            return Ext.String.format('{0} ({1})', value.name, value.lifeCycle);
        } else if(value.all){
            return Uni.I18n.translate('general.all', 'IMT', 'All');
        }
    }
});