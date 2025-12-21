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
	public void getFieldValueTest() throws NoSuchFieldException {
		Field nullableStaticField = ReflectionsTest.class.getDeclaredField("nullableStaticField");
		Field nonNullStaticField = ReflectionsTest.class.getDeclaredField("nonNullStaticField");
		Field nullableNonStaticField = ReflectionsTest.class.getDeclaredField("nullableNonStaticField");
		Field nonNullNonStaticField = ReflectionsTest.class.getDeclaredField("nonNullNonStaticField");

		Object instance = new ReflectionsTest();

		assert  Reflections.getFieldValue(nonNullStaticField) != null;
		assert  Reflections.getFieldValue(instance, nonNullNonStaticField) != null;
		assert  Reflections.getFieldValue(nullableStaticField) == null;
		assert  Reflections.getFieldValue(instance, nullableNonStaticField) == null;
	}

	@Test
	public void setFieldValueTest() throws NoSuchFieldException {
		Field nonNullStaticField = ReflectionsTest.class.getDeclaredField("nonNullStaticField");
		Field nonNullNonStaticField = ReflectionsTest.class.getDeclaredField("nonNullNonStaticField");

		Object instance = new ReflectionsTest();

		assert Reflections.getFieldValue(nonNullStaticField) == "123";
		assert Reflections.getFieldValue(instance, nonNullNonStaticField) == "123";

		Reflections.setFieldValue(nonNullStaticField, "321");
		Reflections.setFieldValue(instance, nonNullNonStaticField, "321");

		assert Reflections.getFieldValue(nonNullStaticField) == "321";
		assert Reflections.getFieldValue(instance, nonNullNonStaticField) == "321";
	}

}
