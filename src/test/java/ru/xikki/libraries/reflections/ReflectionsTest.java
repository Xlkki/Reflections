package ru.xikki.libraries.reflections;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReflectionsTest {

	@Test
	public void unsafeInitTest() {
		Objects.requireNonNull(Reflections.getUnsafe());
	}

	@Test
	public void allocateInstanceTest() throws InstantiationException {
		Objects.requireNonNull(Reflections.getUnsafe());
		Objects.requireNonNull(Reflections.allocateInstance(ReflectionsTest.class));
	}

}
