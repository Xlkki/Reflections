package ru.xikki.libraries.reflections;

import org.junit.jupiter.api.Test;
import ru.xikki.libraries.reflections.condition.FieldCondition;
import ru.xikki.libraries.reflections.condition.MethodCondition;
import ru.xikki.libraries.reflections.iterator.DirectoryClassIterator;
import ru.xikki.libraries.reflections.iterator.JarClassIterator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.jar.JarFile;

public class ReflectionUtilsTests {

	private static String nullableStaticField;
	private static String nonNullStaticField = "123";
	private String nullableNonStaticField;
	private String nonNullNonStaticField = "123";

	@Test
	public void getFieldValueTest() throws NoSuchFieldException {
		Field nullableStaticField = ReflectionUtilsTests.class.getDeclaredField("nullableStaticField");
		Field nonNullStaticField = ReflectionUtilsTests.class.getDeclaredField("nonNullStaticField");
		Field nullableNonStaticField = ReflectionUtilsTests.class.getDeclaredField("nullableNonStaticField");
		Field nonNullNonStaticField = ReflectionUtilsTests.class.getDeclaredField("nonNullNonStaticField");

		Object instance = new ReflectionUtilsTests();

		assert ReflectionUtils.getFieldValue(nonNullStaticField).equals("123");
		assert ReflectionUtils.getFieldValue(instance, nonNullNonStaticField).equals("123");
		assert ReflectionUtils.getFieldValue(nullableStaticField) == null;
		assert ReflectionUtils.getFieldValue(instance, nullableNonStaticField) == null;
	}

	@Test
	public void setFieldValueTest() {
		Object instance = new ReflectionUtilsTests();

		assert ReflectionUtils.getFieldValue(
				ReflectionUtilsTests.class,
				FieldCondition.create()
						.withName("nonNullStaticField")
		).equals("123");
		assert ReflectionUtils.getFieldValue(
				instance,
				FieldCondition.create()
						.withName("nonNullNonStaticField")
		).equals("123");

		ReflectionUtils.setFieldValue(
				ReflectionUtilsTests.class,
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
				ReflectionUtilsTests.class,
				FieldCondition.create()
						.withName("nonNullStaticField")
		).equals("321");
		assert ReflectionUtils.getFieldValue(
				instance, FieldCondition.create()
						.withName("nonNullNonStaticField")
		).equals("321");

		ReflectionUtils.setFieldValue(
				ReflectionUtilsTests.class,
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
		assert !ReflectionUtils.getLoadedClasses(ReflectionUtilsTests.class.getClassLoader()).isEmpty();
	}

	@Test
	public void getSourcePathTest() {
		assert ReflectionUtils.getSourcePath(ReflectionUtilsTests.class).equals(
				Path.of("build\\classes\\java\\test").toAbsolutePath()
		);
	}

	@Test
	public void getCallerClassTest() throws ClassNotFoundException {
		Class<?> clazz = Class.forName("jdk.internal.reflect.DirectMethodHandleAccessor");
		assert ReflectionUtils.getCallerClass() == clazz;
		assert ReflectionUtils.getCallerClass().getName().equals(ReflectionUtils.getCallerClassName());
	}

	@Test
	public void wrapClassTest() {
		assert ReflectionUtils.wrapClass(boolean.class) == Boolean.class;
		assert ReflectionUtils.wrapClass(byte.class) == Byte.class;
		assert ReflectionUtils.wrapClass(short.class) == Short.class;
		assert ReflectionUtils.wrapClass(int.class) == Integer.class;
		assert ReflectionUtils.wrapClass(long.class) == Long.class;
		assert ReflectionUtils.wrapClass(float.class) == Float.class;
		assert ReflectionUtils.wrapClass(double.class) == Double.class;
		assert ReflectionUtils.wrapClass(char.class) == Character.class;
		assert ReflectionUtils.wrapClass(Object.class) == Object.class;
	}

	@Test
	public void createEnumAndUpdateTest() {
		assert TestEnum.values().length == 1;

		TestEnum newValue = ReflectionUtils.createEnum(TestEnum.class, "B");

		assert newValue.ordinal() == 1;
		assert newValue.name().equals("B");
		assert TestEnum.values().length == 2;

		ReflectionUtils.updateEnum(ReflectionUtilsTests.class.getClassLoader(), TestEnum.class);
		assert UpdateEnumTest.test(newValue) == 2;
	}

	@Test
	public void classIteratorTest() {
		Path path = ReflectionUtils.getSourcePath();
		Iterator<String> iterator;
		if (Files.isDirectory(path)) {
			iterator = new DirectoryClassIterator(path);
			assert iterator.hasNext();
		} else {
			try (JarFile file = new JarFile(path.toFile())) {
				iterator = new JarClassIterator(file);
				assert iterator.hasNext();
			} catch (IOException e) {

			}
		}

	}

	static enum TestEnum {

		A;

	}

	static class UpdateEnumTest {

		static int test(TestEnum testEnum) {
			return switch (testEnum) {
				case A -> 1;
				default -> 2;
			};
		}

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
