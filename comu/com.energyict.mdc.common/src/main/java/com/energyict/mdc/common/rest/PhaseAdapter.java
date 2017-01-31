/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.Phase;

public class PhaseAdapter extends MapBasedXmlAdapter<Phase> {

    public PhaseAdapter() {
        register("",Phase.NOTAPPLICABLE );
        register("Not applicable",Phase.NOTAPPLICABLE );
        register("A",Phase.PHASEA );
        register("AA",Phase.PHASEAA );
        register("AB",Phase.PHASEAB );
        register("ABCN",Phase.PHASEABCN );
        register("ABN",Phase.PHASEABN );
        register("AN",Phase.PHASEAN );
        register("B",Phase.PHASEB );
        register("BA",Phase.PHASEBA );
        register("BC",Phase.PHASEBC );
        register("BCN",Phase.PHASEBCN );
        register("BN",Phase.PHASEBN );
        register("C",Phase.PHASEC );
        register("CA",Phase.PHASECA );
        register("CAN",Phase.PHASECAN );
        register("CN",Phase.PHASECN );
        register("N",Phase.PHASEN );
        register("NN",Phase.PHASENTOGND);
        register("ABC",Phase.PHASEABC );
        register("S2",Phase.PHASES2);
        register("S2N",Phase.PHASES2N);
        register("S1",Phase.PHASES1);
        register("S1N",Phase.PHASES1N);
        register("S12",Phase.PHASES12);
        register("S12N",Phase.PHASES12N);
        register("3 wire wye",Phase.PHASETHREEWIREWYE);
        register("4 wire wye",Phase.PHASEFOURWIREWYE);
        register("3 wired delta",Phase.PHASETHREEWIREDELTA);
        register("4 wired delta",Phase.PHASEFOURWIREDELTA);
        register("4 wire HL delta",Phase.PHASEFOURWIREHLDELTA);
        register("4 wire open delta",Phase.PHASEFOURWIREOPENDELTA);
        register("Networked",Phase.PHASENETWORKED);
    }
}
