package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class RepeaterDiagnostic extends AbstractField<RepeaterDiagnostic> {

    public static final int LENGTH = 1;

    private int diagnosticId;
    private Diagnostic diagnostic;

    public RepeaterDiagnostic() {
        this.diagnostic = Diagnostic.UNKNOWN;
    }

    public RepeaterDiagnostic(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(diagnosticId, LENGTH);
    }

    @Override
    public RepeaterDiagnostic parse(byte[] rawData, int offset) throws ParsingException {
        diagnosticId = getIntFromBytesLE(rawData, offset, LENGTH);
        diagnostic = Diagnostic.fromCode(diagnosticId);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getDiagnosticId() {
        return diagnosticId;
    }

    public String getDiagnosticInfo() {
        if (!this.diagnostic.equals(Diagnostic.UNKNOWN)) {
            return diagnostic.getDiagnosticDescription();
        } else {
            return (diagnostic.getDiagnosticDescription() + " " + diagnosticId);
        }
    }

    private enum Diagnostic {

        NOT_REGISTERED(0x00, "Not registered"),
        DOES_NOT_ANSWER(0x01, "Does not answer any commands"),
        ANSWER_RADIO_COMMANDS(0x02, "Does answer radio commands"),
        ANSWER_UCL_INFO(0x03, "Does answer UCL INFO"),
        ANSWER_OPENING_SESSION(0x04, "Does answer opening session"),
        ANSWER_POLlING(0x05, "Does answer polling"),
        ALREADY_CHECKPOINT_CONSULTATION(0x06, "Already had checkpoint consultation"),
        ANSWERED_CHECKPOINT(0x07, "Answered Checkpoint"),
        UNKNOWN(0xFF, "Unknown diagnostic state");

        /**
         * The error code of the error *
         */
        private final int diagnosticCode;

        /**
         * The textual description of the error *
         */
        private final String diagnosticDescription;

        private Diagnostic(int diagnosticCode, String diagnosticDescription) {
            this.diagnosticCode = diagnosticCode;
            this.diagnosticDescription = diagnosticDescription;
        }

        public int getDiagnosticCode() {
            return diagnosticCode;
        }

        public String getDiagnosticDescription() {
            return diagnosticDescription;
        }

        public static Diagnostic fromCode(int code) {
            for (Diagnostic diagnostic : Diagnostic.values()) {
                if (diagnostic.getDiagnosticCode() == code) {
                    return diagnostic;
                }
            }
            return Diagnostic.UNKNOWN;
        }
    }
}