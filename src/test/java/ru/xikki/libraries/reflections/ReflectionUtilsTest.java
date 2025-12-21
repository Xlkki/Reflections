package ru.xikki.libraries.reflections;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtilsTest {

	private static String nullableStaticField;
	private static String nonNullStaticField = "123";
	private String nullableNonStaticField;
	private String nonNullNonStaticField = "123";

	@Test
	public void getFieldValueTest() throws NoSuchFieldException {
		Field nullableStaticField = ReflectionUtilsTest.class.getDeclaredField("nullableStaticField");
		Field nonNullStaticField = ReflectionUtilsTest.class.getDeclaredField("nonNullStaticField");
		Field nullableNonStaticField = ReflectionUtilsTest.class.getDeclaredField("nullableNonStaticField");
		Field nonNullNonStaticField = ReflectionUtilsTest.class.getDeclaredField("nonNullNonStaticField");

		Object instance = new ReflectionUtilsTest();

		assert ReflectionUtils.getFieldValue(nonNullStaticField).equals("123");
		assert ReflectionUtils.getFieldValue(instance, nonNullNonStaticField).equals("123");
		assert ReflectionUtils.getFieldValue(nullableStaticField) == null;
		assert ReflectionUtils.getFieldValue(instance, nullableNonStaticField) == null;
	}

	@Test
	public void setFieldValueTest() {
		Object instance = new ReflectionUtilsTest();

		assert ReflectionUtils.getFieldValue(ReflectionUtilsTest.class, "nonNullStaticField").equals("123");
		assert ReflectionUtils.getFieldValue(instance, "nonNullNonStaticField").equals("123");

		ReflectionUtils.setFieldValue(ReflectionUtilsTest.class, "nonNullStaticField", "321");
		ReflectionUtils.setFieldValue(instance, "nonNullNonStaticField", "321");

		assert ReflectionUtils.getFieldValue(ReflectionUtilsTest.class, "nonNullStaticField").equals("321");
		assert ReflectionUtils.getFieldValue(instance, "nonNullNonStaticField").equals("321");

		ReflectionUtils.setFieldValue(ReflectionUtilsTest.class, "nonNullStaticField", "123");
		ReflectionUtils.setFieldValue(instance, "nonNullNonStaticField", "123");
	}

	@Test
	public void classAccessibleTest() throws NoSuchFieldException {
		Field moduleField = Class.class.getDeclaredField("module");

		try {
			moduleField.setAccessible(true);
			throw new IllegalStateException("Field already accessible");
		} catch (Exception e) {

		}

		ReflectionUtils.setClassAccessible(Class.class);
		moduleField.setAccessible(true);
		moduleField.setAccessible(false);
		ReflectionUtils.resetClassAccessible(Class.class);

		try {
			moduleField.setAccessible(true);
			throw new IllegalStateException("Field already accessible");
		} catch (Exception e) {

		}
	}

	@Test
	public void getFieldsTest() {
		Field[] currentClassFields = ReflectionUtils.getFields(A.B.class);
		Field[] superClassFields = ReflectionUtils.getFields(A.B.class, true);

		assert currentClassFields.length == 1;
		assert superClassFields.length == 2;
	}

	@Test
	public void getFieldsByConditionTest() throws NoSuchFieldException {

		Field field = A.B.class.getDeclaredField("a");
		Field field1 = ReflectionUtils.getField(A.B.class, "a");

		assert field.equals(field1);

		field = A.class.getDeclaredField("a");
		field1 = ReflectionUtils.getField(A.B.class, true, (someField) -> {
			return someField.getName().equals("a") && someField.getDeclaringClass() == A.class;
		});

		assert field.equals(field1);
	}

	@Test
	public void getRecordFieldTest() {
		ReflectionUtils.getFieldOrThrow(TestRecord.class, "a");
	}

	@Test
	public void setRecordFieldValueTest() {
		TestRecord record = new TestRecord(1);

		assert record.a == 1;

		ReflectionUtils.setFieldValue(record, "a", 2);

		assert record.a == 2;
	}

	@Test
	public void getMethodsTest() {
		Method[] currentClassMethods = ReflectionUtils.getMethods(A.B.class);
		Method[] superClassMethods = ReflectionUtils.getMethods(A.B.class, true);

		assert currentClassMethods.length == 1;
		assert superClassMethods.length == 14;
	}

	@Test
	public void getMethodsByConditionTest() throws NoSuchFieldException, NoSuchMethodException {
		Method method = A.B.class.getDeclaredMethod("test");
		Method method1 = ReflectionUtils.getMethod(A.B.class, "test");

		assert method.equals(method1);

		method = A.class.getDeclaredMethod("test");
		method1 = ReflectionUtils.getMethod(A.B.class, true, (someMethod) -> {
			return someMethod.getName().equals("test") && someMethod.getDeclaringClass() == A.class;
		});

		assert method.equals(method1);
	}

	@Test
	public void getLoadedClassesTest() {
		assert !ReflectionUtils.getLoadedClasses(ReflectionUtilsTest.class.getClassLoader()).isEmpty();
	}

	static record TestRecord(int a) {

	}

	static class A {

		private static final int a = 1;

		public static void test() {
			System.out.println("A");
		}

		static class B extends A {

			private static final int a = 2;

			public static void test() {
				System.out.println("B");
			}

		}

	}

}
