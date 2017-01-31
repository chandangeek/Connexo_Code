/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Ext.rtl.dom.Element_static', {
    override: 'Ext.dom.Element',
   
    statics: { 
        rtlUnitizeBox: function(box, units){
            var a = this.addUnits,
                b = this.parseBox(box);

            // Usual order is trbl, so reverse it
            // to return tlbr
            return a(b.top, units) + ' ' +
                   a(b.left, units) + ' ' +
                   a(b.bottom, units) + ' ' +
                   a(b.right, units);
        },
        
        rtlParseBox: function(box){
            var box = Ext.dom.Element.parseBox(box),
                temp;
               
            temp = box.left;
            box.left = box.right;
            box.right = temp;
            
            return box;
        }
    }
});
