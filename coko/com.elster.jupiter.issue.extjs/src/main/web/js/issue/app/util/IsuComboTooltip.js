/**
 * This class is used as a mixin.
 *
 * This class is to be used to provide basic methods for controllers that handle events of combobox.
 */
Ext.define('Isu.util.IsuComboTooltip', {
    /**
     * Sets tooltip for combobox.
     * Combobox must has 'tooltipText' property otherwise it sets default text.
     */
    setComboTooltip: function (combo) {
        combo.tooltip = Ext.DomHelper.append(Ext.getBody(), {
            tag: 'div',
            html: combo.tooltipText || 'Start typing',
            cls: 'isu-combo-tooltip'
        }, true);

        combo.tooltip.hide();

        combo.on('destroy', function () {
            combo.tooltip.destroy();
        });
        combo.on('focus', this.onFocusComboTooltip, this);
        combo.on('blur', this.onBlurComboTooltip, this);
    },

    /**
     * Handle 'focus' event.
     * If value of combobox is null shows tooltip.
     */
    onFocusComboTooltip: function (combo) {
        var tooltip = combo.tooltip,
            comboEl = Ext.get(combo.getEl());

        tooltip.setStyle({
            width: comboEl.getWidth(false) + 'px',
            top: comboEl.getY() + comboEl.getHeight(false) + 'px',
            left: comboEl.getX() + 'px'
        });

        if (!combo.getValue()) {
            tooltip.show();
        }

        combo.on('change', this.clearComboTooltip, this);
    },

    /**
     * Handle 'blur' event.
     * Hides tooltip of combobox on blur.
     */
    onBlurComboTooltip: function (combo) {
        var tooltip = combo.tooltip;

        tooltip && tooltip.hide();

        combo.un('change', this.clearComboTooltip, this);
    },

    /**
     * Handle 'change' event.
     * If value of combobox is null resets combobox and shows tooltip otherwise hides tooltip
     * and shows list of values.
     */
    clearComboTooltip: function (combo, newValue) {
        var listValues = combo.picker,
            tooltip = combo.tooltip;

        if (newValue == null) {
            combo.reset();
            listValues && listValues.hide();
            tooltip && tooltip.show();
        } else {
            tooltip && tooltip.hide();
            listValues && listValues.show();
        }
    }
});
