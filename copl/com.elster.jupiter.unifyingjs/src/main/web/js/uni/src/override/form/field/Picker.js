/**
 * This override fixes an ExtJS 4.2.3 defect where the picker closes early.
 *
 * @markdown
 * @docauthor Rex Staples
 */
Ext.define('Uni.override.form.field.Picker', {
    override: 'Ext.form.field.Picker',

    // Overrides code below come from ExtJS 4.2.2
    // http://docs.sencha.com/extjs/4.2.2/source/Picker.html#Ext-form-field-Picker

    /**
     * @private
     * Runs on mousewheel and mousedown of doc to check to see if we should collapse the picker
     */
    collapseIf: function(e) {
        var me = this;

        if (!me.isDestroyed && !e.within(me.bodyEl, false, true) && !e.within(me.picker.el, false, true) && !me.isEventWithinPickerLoadMask(e)) {
            me.collapse();
        }
    },

    mimicBlur: function(e) {
        var me = this,
            picker = me.picker;
        // ignore mousedown events within the picker element
        if (!picker || !e.within(picker.el, false, true) && !me.isEventWithinPickerLoadMask(e)) {
            me.callParent(arguments);
        }
    },

    /**
     * returns true if the picker has a load mask and the passed event is within the load mask
     * @private
     * @param {Ext.EventObject} e
     * @return {Boolean}
     */
    isEventWithinPickerLoadMask: function(e) {
        var loadMask = this.picker.loadMask;
        return loadMask ? e.within(loadMask.maskEl, false, true) || e.within(loadMask.el, false, true) : false;
    }
});
