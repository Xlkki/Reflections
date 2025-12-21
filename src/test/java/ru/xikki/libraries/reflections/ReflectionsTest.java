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

		assert  Reflections.getFieldValue(nonNullStaticField).equals("123");
		assert  Reflections.getFieldValue(instance, nonNullNonStaticField).equals("123");
		assert  Reflections.getFieldValue(nullableStaticField) == null;
		assert  Reflections.getFieldValue(instance, nullableNonStaticField) == null;
	}

	@Test
	public void setFieldValueTest() throws NoSuchFieldException {
		Field nonNullStaticField = ReflectionsTest.class.getDeclaredField("nonNullStaticField");
		Field nonNullNonStaticField = ReflectionsTest.class.getDeclaredField("nonNullNonStaticField");

		Object instance = new ReflectionsTest();

		assert Reflections.getFieldValue(nonNullStaticField).equals("123");
		assert Reflections.getFieldValue(instance, nonNullNonStaticField).equals("123");

		Reflections.setFieldValue(nonNullStaticField, "321");
		Reflections.setFieldValue(instance, nonNullNonStaticField, "321");

		assert Reflections.getFieldValue(nonNullStaticField).equals("321");
		assert Reflections.getFieldValue(instance, nonNullNonStaticField).equals("321");

		Reflections.setFieldValue(nonNullStaticField, "123");
		Reflections.setFieldValue(instance, nonNullNonStaticField, "123");
	}

	@Test
	public void classAccessibleTest() throws NoSuchFieldException {
		Field moduleField = Class.class.getDeclaredField("module");

		try {
			moduleField.setAccessible(true);
			throw new IllegalStateException("Field already accessible");
		} catch (Exception e) {

		}

		Reflections.setClassAccessible(Class.class);
		moduleField.setAccessible(true);
		moduleField.setAccessible(false);
		Reflections.resetClassAccessible(Class.class);

		try {
			moduleField.setAccessible(true);
			throw new IllegalStateException("Field already accessible");
		} catch (Exception e) {

		}
	}

}
