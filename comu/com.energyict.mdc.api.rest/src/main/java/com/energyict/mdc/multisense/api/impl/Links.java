package com.energyict.mdc.multisense.api.impl;

/**
 * Created by bvn on 4/29/15.
 */
public class Links {
    public Reference self;
    public Reference first;
    public Reference prev;
    public Reference next;
    public Reference last;

    public Links(String href) {
        self=new Reference();
        self.href = href;
    }

    static class Reference {

        public String href;

        public Reference() {
        }

        public Reference(String href) {
            this.href = href;
        }
    }
}
