package ru.xikki.libraries.reflections;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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

		assert Reflections.getFieldValue(nonNullStaticField).equals("123");
		assert Reflections.getFieldValue(instance, nonNullNonStaticField).equals("123");
		assert Reflections.getFieldValue(nullableStaticField) == null;
		assert Reflections.getFieldValue(instance, nullableNonStaticField) == null;
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

	@Test
	public void getFieldsTest() {
		Field[] currentClassFields = Reflections.getFields(A.B.class);
		Field[] superClassFields = Reflections.getFields(A.B.class, true);

		assert currentClassFields.length == 1;
		assert superClassFields.length == 2;
	}

	@Test
	public void getFieldsByConditionTest() throws NoSuchFieldException {

		Field field = A.B.class.getDeclaredField("a");
		Field field1 = Reflections.getField(A.B.class, (someField) -> someField.getName().equals("a"));

		assert field.equals(field1);

		field = A.class.getDeclaredField("a");
		field1 = Reflections.getField(A.B.class, true, (someField) -> {
			return someField.getName().equals("a") && someField.getDeclaringClass() == A.class;
		});

		assert field.equals(field1);
	}

	static class A {

		private static final int a = 1;

		static class B extends A {

			private static final int a = 2;

		}

	}

}
