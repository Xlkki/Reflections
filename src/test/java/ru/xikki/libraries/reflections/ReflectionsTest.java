package ru.xikki.libraries.reflections;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Objects;

public class ReflectionsTest {

	private static String nullableStaticField;
	private static String nonNullStaticField = "123";
	private String nullableNonStaticField;
	private String nonNullNonStaticField = "123";

	@Test
	public void unsafeInitTest() {
		Objects.requireNonNull(Reflections.getUnsafe());
	}

	@Test
	public void allocateInstanceTest() throws InstantiationException {
		Objects.requireNonNull(Reflections.getUnsafe());
		Objects.requireNonNull(Reflections.allocateInstance(ReflectionsTest.class));
	}

	@Test
	public void getFieldValueTest() throws NoSuchFieldException {
		Field nullableStaticField = ReflectionsTest.class.getDeclaredField("nullableStaticField");
		Field nonNullStaticField = ReflectionsTest.class.getDeclaredField("nonNullStaticField");
		Field nullableNonStaticField = ReflectionsTest.class.getDeclaredField("nullableNonStaticField");
		Field nonNullNonStaticField = ReflectionsTest.class.getDeclaredField("nonNullNonStaticField");

		Object instance = new ReflectionsTest();

		Objects.requireNonNull(Reflections.getFieldValue(nonNullStaticField));
		Objects.requireNonNull(Reflections.getFieldValue(instance, nonNullNonStaticField));

		if (Reflections.getFieldValue(nullableStaticField) != null) {
			throw new IllegalArgumentException("Static field value is not null");
		}
		if (Reflections.getFieldValue(instance, nullableNonStaticField) != null) {
			throw new IllegalArgumentException("Non-static field value is not null");
		}
	}

}
