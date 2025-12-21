package ru.xikki.libraries.reflections;

import org.junit.jupiter.api.Test;
import ru.xikki.libraries.reflections.condition.FieldCondition;
import ru.xikki.libraries.reflections.condition.MethodCondition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

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

		assert ReflectionUtils.getFieldValue(
				ReflectionUtilsTest.class,
				FieldCondition.create()
						.withName("nonNullStaticField")
		).equals("123");
		assert ReflectionUtils.getFieldValue(
				instance,
				FieldCondition.create()
						.withName("nonNullNonStaticField")
		).equals("123");

		ReflectionUtils.setFieldValue(
				ReflectionUtilsTest.class,
				FieldCondition.create()
						.withName("nonNullStaticField"),
				"321"
		);
		ReflectionUtils.setFieldValue(
				instance,
				FieldCondition.create()
						.withName("nonNullNonStaticField"),
				"321"
		);

		assert ReflectionUtils.getFieldValue(
				ReflectionUtilsTest.class,
				FieldCondition.create()
						.withName("nonNullStaticField")
		).equals("321");
		assert ReflectionUtils.getFieldValue(
				instance, FieldCondition.create()
						.withName("nonNullNonStaticField")
		).equals("321");

		ReflectionUtils.setFieldValue(
				ReflectionUtilsTest.class,
				FieldCondition.create()
						.withName("nonNullStaticField"),
				"123"
		);
		ReflectionUtils.setFieldValue(
				instance,
				FieldCondition.create()
						.withName("nonNullNonStaticField"),
				"123"
		);
	}

	@Test
	public void classAccessibleTest() {
		Field moduleField = ReflectionUtils.getFieldOrThrow(
				Class.class,
				FieldCondition.create()
						.withName("module")
						.withStatic(false)
		);

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
		Field field1 = ReflectionUtils.getField(
				A.B.class,
				FieldCondition.create()
						.withName("a")
		);

		assert field.equals(field1);

		field = A.class.getDeclaredField("a");
		field1 = ReflectionUtils.getField(
				A.B.class,
				true,
				FieldCondition.create()
						.withName("a")
						.withDeclaredClass(A.class)
		);

		assert field.equals(field1);
	}

	@Test
	public void getRecordFieldTest() {
		ReflectionUtils.getFieldOrThrow(
				TestRecord.class,
				FieldCondition.create()
						.withName("a")
		);
	}

	@Test
	public void setRecordFieldValueTest() {
		TestRecord record = new TestRecord(1);

		assert record.a == 1;

		ReflectionUtils.setFieldValue(
				record,
				FieldCondition.create()
						.withName("a"),
				2
		);

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
		Method method1 = ReflectionUtils.getMethod(
				A.B.class,
				MethodCondition.create()
						.withName("test")
		);

		assert method.equals(method1);

		method = A.class.getDeclaredMethod("test");
		method1 = ReflectionUtils.getMethod(
				A.B.class,
				true,
				MethodCondition.create()
						.withName("test")
						.withDeclaredClass(A.class)
		);

		assert method.equals(method1);
	}

	@Test
	public void getLoadedClassesTest() {
		assert !ReflectionUtils.getLoadedClasses(ReflectionUtilsTest.class.getClassLoader()).isEmpty();
	}

	@Test
	public void getSourcePathTest() {
		assert ReflectionUtils.getSourcePath(ReflectionUtilsTest.class).equals(
				Path.of("build\\classes\\java\\test").toAbsolutePath()
		);
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
