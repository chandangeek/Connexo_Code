/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by dvy on 21/05/2015.
 */
Ext.define('Uni.override.DisplayFieldOverride', {
    override: 'Ext.form.field.Display',
    emptyValueDisplay: '-',
    labelWidth: 200,

    initComponent: function () {
        this.callParent(arguments);
        this.on('refresh', this.setTooltip);
        this.on('resize', this.setTooltip);
    },

    /**
     * @private
     */
    setTooltip: function (field) {
        if (field.rendered && !field.isHidden()) {
            var inputEl = field.getEl().down('#'+field.id+'-inputEl'),
                tm = Ext.isEmpty(inputEl) ? null : new Ext.util.TextMetrics(inputEl),
                value = Ext.isEmpty(inputEl) ? null : inputEl.dom.innerHTML;

            if (inputEl !== null && inputEl.getWidth() < tm.getWidth(value)) {
                Ext.suspendLayouts();
                inputEl.set({'data-qtip': value});
                Ext.resumeLayouts(true);
            }

        }
    },

    renderer: function(value){
        if(Ext.isEmpty(value)) {
            return this.emptyValueDisplay;
        }
        return this.htmlEncode ? Ext.String.htmlEncode(value) : value;

    },
    htmlEncode: true // this setting is only applied when you have no renderer defined
});