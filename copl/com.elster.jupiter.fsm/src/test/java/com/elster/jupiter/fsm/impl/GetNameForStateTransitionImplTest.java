/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Thesaurus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the getName method of the {@link StateTransitionImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-24 (16:38)
 */
@RunWith(MockitoJUnitRunner.class)
public class GetNameForStateTransitionImplTest {

    private static final String EVENT_TYPE_SYMBOL = "#test";
    private static final String COMMON_TRANSLATION = "Translated";

    @Mock
    private FiniteStateMachine stateMachine;
    @Mock
    private State from;
    @Mock
    private State to;
    @Mock
    private StateTransitionEventType eventType;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void initializeMocks() {
        when(this.eventType.getSymbol()).thenReturn(EVENT_TYPE_SYMBOL);
        when(this.thesaurus.getString(anyString(), anyString())).thenReturn(COMMON_TRANSLATION);
    }

    @Test
    public void doesNotDelegateToThesaurusWhenNameIsNotEmpty() {
        StateTransitionImpl stateTransition = this.getTestInstance();
        String expectedName = "doesNotDelegateToThesaurusWhenNameIsNotEmpty";
        stateTransition.setName(expectedName);

        // Business method
        String name = stateTransition.getName(this.thesaurus);

        // Asserts
        verify(this.thesaurus, never()).getString(anyString(), anyString());
        assertThat(name).isEqualTo(expectedName);
    }

    @Test
    public void delegatesToThesaurusWithEventTypeSymbolWhenNameAndTranslationNameKeyAreEmpty() {
        StateTransitionImpl stateTransition = this.getTestInstance();

        // Business method
        String name = stateTransition.getName(this.thesaurus);

        // Asserts
        verify(this.thesaurus).getString(eq(EVENT_TYPE_SYMBOL), anyString());
        assertThat(name).isNotEmpty();
    }

    @Test
    public void delegatesToThesaurusWhenTranslationNameKeyIsNotEmpty() {
        StateTransitionImpl stateTransition = this.getTestInstance();
        String translationKey = "delegatesToThesaurusWhenTranslationNameKeyIsNotEmpty";
        stateTransition.setTranslationKey(translationKey);

        // Business method
        String name = stateTransition.getName(this.thesaurus);

        // Asserts
        verify(this.thesaurus).getString(eq(translationKey), anyString());
        assertThat(name).isNotEmpty();
    }

    private StateTransitionImpl getTestInstance() {
        return new StateTransitionImpl().initialize(this.stateMachine, this.from, this.to, this.eventType);
    }

}