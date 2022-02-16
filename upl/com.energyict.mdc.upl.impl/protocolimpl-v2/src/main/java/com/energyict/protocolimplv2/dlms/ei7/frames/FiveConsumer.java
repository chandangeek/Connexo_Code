package com.energyict.protocolimplv2.dlms.ei7.frames;

import java.util.Objects;

@FunctionalInterface
public interface FiveConsumer<A, B, C, D, E> {
	void accept(A a, B b, C c, D d, E e);

	default FiveConsumer<A, B, C, D, E> andThen(FiveConsumer<? super A, ? super B, ? super C, ? super D, ? super E> after) {
		Objects.requireNonNull(after);

		return (a, b, c, d, e) -> {
			accept(a, b, c, d, e);
			after.accept(a, b, c, d, e);
		};
	}
}
