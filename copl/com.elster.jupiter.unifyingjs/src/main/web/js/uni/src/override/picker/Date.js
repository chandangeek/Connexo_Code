/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.picker.Date', {
    override: 'Ext.picker.Date',

    todayText: Uni.I18n.translate('datePicker.todayText', 'UNI', 'Today'),
    nextText: Uni.I18n.translate('datePicker.nextText', 'UNI', 'Next Month (Control+Right)'),
    prevText: Uni.I18n.translate('datePicker.prevText', 'UNI', 'Previous Month (Control+Left)'),
    monthYearText: Uni.I18n.translate('datePicker.monthYearText', 'UNI', 'Choose a month (Control+Up/Down to move years)'),
    disabledDatesText: Uni.I18n.translate('datePicker.disabledDatesText', 'UNI', 'Disabled'),
    disabledDaysText: Uni.I18n.translate('datePicker.disabledDaysText', 'UNI', 'Disabled'),
    maxText: Uni.I18n.translate('datePicker.maxText', 'UNI', 'This date is after the maximum date'),
    minText: Uni.I18n.translate('datePicker.minText', 'UNI', 'This date is before the minimum date'),

    runAnimation: function(isHide){
        var me = this,
            picker = this.monthPicker,
            options = {
                duration: 200,
                callback: function() {
                    picker.setVisible(!isHide);
                    // See showMonthPicker
                    picker.ownerCmp = isHide ? null : me;
                }
            };

        if (isHide) {
            picker.el.slideOut('t', options);
        } else {
            picker.el.slideIn('t', options);
        }
    },

    hideMonthPicker: function(animate){
        var me = this,
            picker = me.monthPicker;

        if (picker && picker.isVisible()) {
            if (me.shouldAnimate(animate)) {
                me.runAnimation(true);
            } else {
                picker.hide();
                // See showMonthPicker
                picker.ownerCmp = null;
            }
        }
        return me;
    },

    showMonthPicker: function(animate) {
        var me = this,
            el = me.el,
            picker;

        if (me.rendered && !me.disabled) {
            picker = me.createMonthPicker();
            if (!picker.isVisible()) {
                picker.setValue(me.getActive());
                picker.setSize(el.getSize());
                picker.setPosition(-el.getBorderWidth('l'), -el.getBorderWidth('t'));
                if (me.shouldAnimate(animate)) {
                    me.runAnimation(false);
                } else {
                    picker.show();
                    // We need to set the ownerCmp so that owns() can correctly
                    // match up the component hierarchy, however when positioning the picker
                    // we don't want it to position like a normal floater because we render it to
                    // month picker element itself.
                    picker.ownerCmp = me;
                }
            }
        }
        return me;
    }
});