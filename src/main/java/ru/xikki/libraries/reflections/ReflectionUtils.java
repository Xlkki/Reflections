package ru.xikki.libraries.reflections;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.xikki.libraries.reflections.condition.FieldCondition;
import ru.xikki.libraries.reflections.condition.MethodCondition;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@UtilityClass
public class ReflectionUtils {

	private final Unsafe UNSAFE;
	private final Object INTERNAL_UNSAFE;

	private final Field MODULE_FIELD;
	private final Field CLASSES_FIELD;

	private final Method GET_DECLARED_FIELDS_METHOD;
	private final Method GET_DECLARED_METHODS_METHOD;
	private final Method OBJECT_FIELD_OFFSET_METHOD;
	private final Method STATIC_FIELD_OFFSET_METHOD;

	private final Map<Class<?>, Module> DEFAULT_MODULES;

	private boolean defaultIncludeSuperClassFields = false;
	private boolean defaultIncludeSuperClassMethods = false;

	static {
		try {
			DEFAULT_MODULES = new HashMap<>();

			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);

			MODULE_FIELD = Class.class.getDeclaredField("module");

			ReflectionUtils.setClassAccessible(Class.class);
			GET_DECLARED_FIELDS_METHOD = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
			GET_DECLARED_METHODS_METHOD = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
			GET_DECLARED_FIELDS_METHOD.setAccessible(true);
			GET_DECLARED_METHODS_METHOD.setAccessible(true);
			ReflectionUtils.resetClassAccessible(Class.class);

			ReflectionUtils.setClassAccessible(Unsafe.class);
			INTERNAL_UNSAFE = ReflectionUtils.getFieldValueOrThrow(
					Unsafe.class,
					FieldCondition.create()
							.withStatic(true)
							.withName("theInternalUnsafe")
			);
			ReflectionUtils.resetClassAccessible(Unsafe.class);

			ReflectionUtils.setClassAccessible(INTERNAL_UNSAFE.getClass());
			OBJECT_FIELD_OFFSET_METHOD = ReflectionUtils.getMethodOrThrow(
					INTERNAL_UNSAFE.getClass(),
					false,
					MethodCondition.create()
							.withName("objectFieldOffset")
							.withStatic(false)
							.withParametersCount(1)
							.withParameter(0, Field.class)
			);
			STATIC_FIELD_OFFSET_METHOD = ReflectionUtils.getMethodOrThrow(
					INTERNAL_UNSAFE.getClass(),
					false,
					MethodCondition.create()
							.withName("staticFieldOffset")
							.withStatic(false)
							.withParametersCount(1)
							.withParameter(0, Field.class)
			);

			OBJECT_FIELD_OFFSET_METHOD.setAccessible(true);
			STATIC_FIELD_OFFSET_METHOD.setAccessible(true);
			ReflectionUtils.resetClassAccessible(INTERNAL_UNSAFE.getClass());

			ReflectionUtils.setClassAccessible(ClassLoader.class);
			CLASSES_FIELD = ReflectionUtils.getFieldOrThrow(
					ClassLoader.class,
					FieldCondition.create()
							.withStatic(false)
							.withName("classes")
			);
			CLASSES_FIELD.setAccessible(true);
			ReflectionUtils.resetClassAccessible(ClassLoader.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get default includeSuper parameter in `getField` and `getFields` methods
	 *
	 * @return true if `getField` and `getFields` methods return super class fields, otherwise - false
	 *
	 */
	public static boolean getDefaultIncludeSuperClassFields() {
		return defaultIncludeSuperClassFields;
	}

	/**
	 * Set default includeSuper parameter in `getField` and `getFields` methods
	 *
	 * @param defaultIncludeSuperClassFields new includeSuper parameter value
	 *                                       (true if `getField` and `getFields` methods return super class fields, otherwise - false)
	 *
	 */
	public static void setDefaultIncludeSuperClassFields(boolean defaultIncludeSuperClassFields) {
		ReflectionUtils.defaultIncludeSuperClassFields = defaultIncludeSuperClassFields;
	}

	/**
	 * Get default includeSuper parameter in `getMethod` and `getMethods` methods
	 *
	 * @return true if `getMethod` and `getMethods` methods return super class methods, otherwise - false
	 *
	 */
	public static boolean getDefaultIncludeSuperClassMethods() {
		return defaultIncludeSuperClassMethods;
	}

	/**
	 * Set default includeSuper parameter in `getMethod` and `getMethods` methods
	 *
	 * @param defaultIncludeSuperClassMethods new includeSuper parameter value
	 *                                        (true if `getMethod` and `getMethods` methods return super class methods, otherwise - false)
	 *
	 */
	public static void setDefaultIncludeSuperClassMethods(boolean defaultIncludeSuperClassMethods) {
		ReflectionUtils.defaultIncludeSuperClassMethods = defaultIncludeSuperClassMethods;
	}

	/**
	 * Get unsafe instance
	 *
	 * @return Unsafe instance
	 *
	 */
	@NonNull
	public Unsafe getUnsafe() {
		return UNSAFE;
	}

	/**
	 * Init class (calling it static block if class is not initialized)
	 *
	 * @param clazz Class that should be initialized
	 *
	 */
	@SneakyThrows
	public void initClass(@NonNull Class<?> clazz) {
		Class.forName(clazz.getName(), true, clazz.getClassLoader());
	}

	/**
	 * Create empty instance of specified class
	 *
	 * @param clazz Class instance of which should be created
	 * @return Instance of specified class
	 * @throws InstantiationException If specified class is interface or abstract
	 *
	 */
	@NonNull
	public <E> E allocateInstance(@NonNull Class<E> clazz) throws InstantiationException {
		return clazz.cast(UNSAFE.allocateInstance(clazz));
	}

	@SneakyThrows
	private long getObjectFieldOffset(@NonNull Field field) {
		if (!field.getDeclaringClass().isRecord()) {
			return UNSAFE.objectFieldOffset(field);
		} else {
			return (long) OBJECT_FIELD_OFFSET_METHOD.invoke(INTERNAL_UNSAFE, field);
		}
	}

	@SneakyThrows
	private long getStaticFieldOffset(@NonNull Field field) {
		if (!field.getDeclaringClass().isRecord()) {
			return UNSAFE.staticFieldOffset(field);
		} else {
			return (long) STATIC_FIELD_OFFSET_METHOD.invoke(INTERNAL_UNSAFE, field);
		}
	}

	/**
	 * Get static field value
	 *
	 * @param field Field value of which should be returned
	 * @return Value of static field (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Field field) {
		ReflectionUtils.initClass(field.getDeclaringClass());
		Object base = UNSAFE.staticFieldBase(field);
		long offset = ReflectionUtils.getStaticFieldOffset(field);
		return ReflectionUtils.getFieldValue(field, base, offset);
	}

	/**
	 * Get static field value wrapped in optional
	 *
	 * @param field Field value of which should be returned
	 * @return Optional with value of static field
	 *
	 */
	@NonNull
	public Optional<Object> getOptionalFieldValue(@NonNull Field field) {
		return Optional.ofNullable(ReflectionUtils.getFieldValue(field));
	}

	/**
	 * Get static field value. If value is null - throw exception
	 *
	 * @param field Field value of which should be returned
	 * @return Value of static field
	 *
	 */
	@NonNull
	public Object getFieldValueOrThrow(@NonNull Field field) {
		return ReflectionUtils.getOptionalFieldValue(field)
				.orElseThrow();
	}

	/**
	 * Get static field value
	 *
	 * @param clazz     Field declared class
	 * @param condition Field condition
	 * @return Value of static field (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Class<?> clazz, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFieldValue(ReflectionUtils.getFieldOrThrow(clazz, condition));
	}

	/**
	 * Get static field value wrapped in optional
	 *
	 * @param clazz     Field declared class
	 * @param condition Field condition
	 * @return Optional with value of static field
	 *
	 */
	@NonNull
	public Optional<Object> getOptionalFieldValue(@NonNull Class<?> clazz, @NonNull FieldCondition condition) {
		return ReflectionUtils.getOptionalFieldValue(ReflectionUtils.getFieldOrThrow(clazz, condition));
	}

	/**
	 * Get static field value. If value is null - throw exception
	 *
	 * @param clazz     Field declared class
	 * @param condition Field condition
	 * @return Value of static field
	 *
	 */
	@NonNull
	public Object getFieldValueOrThrow(@NonNull Class<?> clazz, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFieldValueOrThrow(ReflectionUtils.getFieldOrThrow(clazz, condition));
	}

	/**
	 * Get non-static field value
	 *
	 * @param instance Instance of field declared class
	 * @param field    Field value of which should be returned
	 * @return Value of non-static field (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Object instance, @NonNull Field field) {
		ReflectionUtils.initClass(field.getDeclaringClass());
		long offset = ReflectionUtils.getObjectFieldOffset(field);
		return ReflectionUtils.getFieldValue(field, instance, offset);
	}

	/**
	 * Get non-static field value wrapped in optional
	 *
	 * @param instance Instance of field declared class
	 * @param field    Field value of which should be returned
	 * @return Optional with value of non-static field
	 *
	 */
	@NonNull
	public Optional<Object> getOptionalFieldValue(@NonNull Object instance, @NonNull Field field) {
		return Optional.ofNullable(ReflectionUtils.getFieldValue(instance, field));
	}

	/**
	 * Get non-static field value. If value is null - throw exception
	 *
	 * @param instance Instance of field declared class
	 * @param field    Field value of which should be returned
	 * @return Value of non-static field
	 *
	 */
	@NonNull
	public Object getFieldValueOrThrow(@NonNull Object instance, @NonNull Field field) {
		return ReflectionUtils.getOptionalFieldValue(instance, field)
				.orElseThrow();
	}

	/**
	 * Get non-static field value
	 *
	 * @param instance  Instance of field declared class
	 * @param condition Field condition
	 * @return Value of non-static field (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Object instance, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFieldValue(instance, ReflectionUtils.getFieldOrThrow(instance.getClass(), condition));
	}

	/**
	 * Get non-static field value wrapped in optional
	 *
	 * @param instance  Instance of field declared class
	 * @param condition Field condition
	 * @return Optional with value of non-static field
	 *
	 */
	@NonNull
	public Optional<Object> getOptionalFieldValue(@NonNull Object instance, @NonNull FieldCondition condition) {
		return ReflectionUtils.getOptionalFieldValue(instance, ReflectionUtils.getFieldOrThrow(instance.getClass(), condition));
	}

	/**
	 * Get non-static field value. If value is null - throw exception
	 *
	 * @param instance  Instance of field declared class
	 * @param condition Field condition
	 * @return Value of non-static field
	 *
	 */
	@NonNull
	public Object getFieldValueOrThrow(@NonNull Object instance, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFieldValueOrThrow(instance, ReflectionUtils.getFieldOrThrow(instance.getClass(), condition));
	}

	private Object getFieldValue(@NonNull Field field, @NonNull Object base, long offset) {
		Class<?> type = field.getType();
		if (!type.isPrimitive()) {
			return UNSAFE.getObject(base, offset);
		} else if (type == boolean.class) {
			return UNSAFE.getBoolean(base, offset);
		} else if (type == byte.class) {
			return UNSAFE.getByte(base, offset);
		} else if (type == short.class) {
			return UNSAFE.getShort(base, offset);
		} else if (type == int.class) {
			return UNSAFE.getInt(base, offset);
		} else if (type == long.class) {
			return UNSAFE.getLong(base, offset);
		} else if (type == float.class) {
			return UNSAFE.getFloat(base, offset);
		} else if (type == double.class) {
			return UNSAFE.getDouble(base, offset);
		} else if (type == char.class) {
			return UNSAFE.getChar(base, offset);
		} else {
			throw new IllegalArgumentException("Can not get value of field with primary type %s".formatted(type.getSimpleName()));
		}
	}

	/**
	 * Set static field value
	 *
	 * @param field Field value of which should be changed
	 * @param value New field value (maybe null)
	 */
	public void setFieldValue(@NonNull Field field, Object value) {
		ReflectionUtils.initClass(field.getDeclaringClass());
		Object base = UNSAFE.staticFieldBase(field);
		long offset = ReflectionUtils.getStaticFieldOffset(field);
		ReflectionUtils.setFieldValue(field, base, offset, value);
	}

	/**
	 * Set static field value
	 *
	 * @param clazz     Field declared class
	 * @param condition Field condition
	 * @param value     New field value (maybe null)
	 *
	 */
	public void setFieldValue(@NonNull Class<?> clazz, @NonNull FieldCondition condition, Object value) {
		ReflectionUtils.setFieldValue(ReflectionUtils.getFieldOrThrow(clazz, condition), value);
	}

	/**
	 * Set non-static field value
	 *
	 * @param instance Instance of field declared class
	 * @param field    Field value of which should be changed
	 * @param value    New field value (maybe null)
	 *
	 */
	public void setFieldValue(@NonNull Object instance, @NonNull Field field, Object value) {
		ReflectionUtils.initClass(field.getDeclaringClass());
		long offset = ReflectionUtils.getObjectFieldOffset(field);
		ReflectionUtils.setFieldValue(field, instance, offset, value);
	}

	/**
	 * Set non-static field value
	 *
	 * @param instance  Instance of field declared class
	 * @param condition Field condition
	 * @param value     New field value (maybe null)
	 *
	 *
	 */
	public void setFieldValue(@NonNull Object instance, @NonNull FieldCondition condition, Object value) {
		ReflectionUtils.setFieldValue(instance, ReflectionUtils.getFieldOrThrow(instance.getClass(), condition), value);
	}

	private void setFieldValue(@NonNull Field field, @NonNull Object base, long offset, Object value) {
		Class<?> type = field.getType();
		if (!type.isPrimitive()) {
			UNSAFE.putObject(base, offset, value);
		} else if (type == boolean.class) {
			UNSAFE.putBoolean(base, offset, (boolean) value);
		} else if (type == byte.class) {
			UNSAFE.putByte(base, offset, (byte) value);
		} else if (type == short.class) {
			UNSAFE.putShort(base, offset, (short) value);
		} else if (type == int.class) {
			UNSAFE.putInt(base, offset, (int) value);
		} else if (type == long.class) {
			UNSAFE.putLong(base, offset, (long) value);
		} else if (type == float.class) {
			UNSAFE.putFloat(base, offset, (float) value);
		} else if (type == double.class) {
			UNSAFE.putDouble(base, offset, (double) value);
		} else {
			UNSAFE.putChar(base, offset, (char) value);
		}
	}

	/**
	 * Set class accessible (Allow to get access to java internal field and methods)
	 *
	 * @param clazz Class that should become available
	 *
	 */
	public void setClassAccessible(@NonNull Class<?> clazz) {
		if (DEFAULT_MODULES.containsKey(clazz)) {
			return;
		}
		Module module = clazz.getModule();
		DEFAULT_MODULES.put(clazz, module);
		ReflectionUtils.setFieldValue(clazz, MODULE_FIELD, ReflectionUtils.class.getModule());
	}

	/**
	 * Reset class accessibility to default
	 *
	 * @param clazz Class accessibility of which should be reset
	 */
	public void resetClassAccessible(@NonNull Class<?> clazz) {
		Module defaultModule = DEFAULT_MODULES.remove(clazz);
		if (defaultModule == null) {
			return;
		}
		ReflectionUtils.setFieldValue(clazz, MODULE_FIELD, defaultModule);
	}

	/**
	 * Get specified class fields (may include super class fields)
	 *
	 * @param clazz        Class fields of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @return Array with fields
	 *
	 */
	@NonNull
	@SneakyThrows
	public Field[] getFields(@NonNull Class<?> clazz, boolean includeSuper) {
		Field[] currentClassFields = (Field[]) GET_DECLARED_FIELDS_METHOD.invoke(clazz, false);
		if (!includeSuper) {
			return currentClassFields;
		} else {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				return currentClassFields;
			}
			Field[] superClassFields = ReflectionUtils.getFields(superClass, true);
			return Stream.concat(Arrays.stream(currentClassFields), Arrays.stream(superClassFields)).toArray(Field[]::new);
		}
	}

	/**
	 * Get specified class fields with default `includeSuper` parameter value
	 *
	 * @param clazz Class fields of which should be returned
	 * @return Array with fields
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz) {
		return ReflectionUtils.getFields(clazz, ReflectionUtils.getDefaultIncludeSuperClassFields());
	}

	@NonNull
	private Stream<Field> getFields0(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Field> condition) {
		return Arrays.stream(ReflectionUtils.getFields(clazz, includeSuper)).filter(condition);
	}

	/**
	 * Get specified class fields by condition (may include super class fields)
	 *
	 * @param clazz        Class fields of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Array with fields
	 *
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getFields0(clazz, includeSuper, condition).toArray(Field[]::new);
	}

	/**
	 * Get specified class fields by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class fields of which should be returned
	 * @param condition Field condition
	 * @return Array with fields
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getFields(clazz, ReflectionUtils.getDefaultIncludeSuperClassFields(), condition);
	}

	/**
	 * Get specified class fields by condition (may include super class fields)
	 *
	 * @param clazz        Class fields of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Array with fields by condition
	 *
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz, boolean includeSuper, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFields(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get specified class fields by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class fields of which should be returned
	 * @param condition Field condition
	 * @return Array with fields by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFields(clazz, ReflectionUtils.getDefaultIncludeSuperClassFields(), condition);
	}

	/**
	 * Get optional with first field from specified class by condition (may include super class fields)
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Optional with field by condition
	 *
	 */
	@NonNull
	public Optional<Field> getOptionalField(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getFields0(clazz, includeSuper, condition).findFirst();
	}

	/**
	 * Get optional with first field from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Optional with field by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Optional<Field> getOptionalField(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getOptionalField(clazz, ReflectionUtils.getDefaultIncludeSuperClassFields(), condition);
	}

	/**
	 * Get optional with first field from specified class by condition (may include super class fields)
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Optional with field by condition
	 *
	 */
	@NonNull
	public Optional<Field> getOptionalField(@NonNull Class<?> clazz, boolean includeSuper, @NonNull FieldCondition condition) {
		return ReflectionUtils.getOptionalField(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get optional with first field from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Optional with field by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Optional<Field> getOptionalField(@NonNull Class<?> clazz, @NonNull FieldCondition condition) {
		return ReflectionUtils.getOptionalField(clazz, ReflectionUtils.getDefaultIncludeSuperClassFields(), condition);
	}

	/**
	 * Get first field from specified class by condition (may include super class fields)
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Field by condition (or null)
	 *
	 */
	public Field getField(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getOptionalField(clazz, includeSuper, condition).orElse(null);
	}

	/**
	 * Get first field from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Field by condition (or null)
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	public Field getField(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getOptionalField(clazz, condition).orElse(null);
	}

	/**
	 * Get first field from specified class by condition (may include super class fields)
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Field by condition (or null)
	 *
	 */
	public Field getField(@NonNull Class<?> clazz, boolean includeSuper, @NonNull FieldCondition condition) {
		return ReflectionUtils.getField(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get first field from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Field by condition (or null)
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	public Field getField(@NonNull Class<?> clazz, @NonNull FieldCondition condition) {
		return ReflectionUtils.getField(clazz, ReflectionUtils.getDefaultIncludeSuperClassFields(), condition);
	}

	/**
	 * Get first field from specified class by condition (may include super class fields). If field not found, throw exception
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Field by condition
	 *
	 */
	@NonNull
	public Field getFieldOrThrow(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getOptionalField(clazz, includeSuper, condition).orElseThrow();
	}

	/**
	 * Get first field from specified class by condition with default `includeSuper` parameter value. If field not found, throw exception
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Field by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 */
	@NonNull
	public Field getFieldOrThrow(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return ReflectionUtils.getOptionalField(clazz, condition).orElseThrow();
	}

	/**
	 * Get first field from specified class by condition (may include super class fields). If field not found, throw exception
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param condition    Field condition
	 * @return Field by condition
	 *
	 */
	@NonNull
	public Field getFieldOrThrow(@NonNull Class<?> clazz, boolean includeSuper, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFieldOrThrow(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get first field from specified class by condition with default `includeSuper` parameter value. If field not found, throw exception
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Field by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassFields(boolean)
	 */
	@NonNull
	public Field getFieldOrThrow(@NonNull Class<?> clazz, @NonNull FieldCondition condition) {
		return ReflectionUtils.getFieldOrThrow(clazz, ReflectionUtils.getDefaultIncludeSuperClassFields(), condition);
	}

	/**
	 * Get specified class methods (may include super class methods)
	 *
	 * @param clazz        Class methods of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @return Array with methods
	 *
	 */
	@NonNull
	@SneakyThrows
	public Method[] getMethods(@NonNull Class<?> clazz, boolean includeSuper) {
		Method[] currentClassMethods = (Method[]) GET_DECLARED_METHODS_METHOD.invoke(clazz, false);
		if (!includeSuper) {
			return currentClassMethods;
		} else {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				return currentClassMethods;
			}
			Method[] superClassMethods = ReflectionUtils.getMethods(superClass, true);
			return Stream.concat(Arrays.stream(currentClassMethods), Arrays.stream(superClassMethods)).toArray(Method[]::new);
		}
	}

	/**
	 * Get specified class methods with default `includeSuper` parameter value
	 *
	 * @param clazz Class methods of which should be returned
	 * @return Array with methods
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz) {
		return ReflectionUtils.getMethods(clazz, ReflectionUtils.getDefaultIncludeSuperClassMethods());
	}

	@NonNull
	private Stream<Method> getMethods0(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Method> condition) {
		return Arrays.stream(ReflectionUtils.getMethods(clazz, includeSuper)).filter(condition);
	}

	/**
	 * Get specified class methods by condition (may include super class methods)
	 *
	 * @param clazz        Class methods of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Array with methods
	 *
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getMethods0(clazz, includeSuper, condition).toArray(Method[]::new);
	}

	/**
	 * Get specified class methods by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class methods of which should be returned
	 * @param condition Method condition
	 * @return Array with methods
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getMethods(clazz, ReflectionUtils.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get specified class methods by condition (may include super class methods)
	 *
	 * @param clazz        Class methods of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Array with methods by condition
	 *
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz, boolean includeSuper, @NonNull MethodCondition condition) {
		return ReflectionUtils.getMethods(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get specified class methods by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class methods of which should be returned
	 * @param condition Method condition
	 * @return Array with methods by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz, @NonNull MethodCondition condition) {
		return ReflectionUtils.getMethods(clazz, ReflectionUtils.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get optional with first method from specified class by condition (may include super class methods)
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Optional with method by condition
	 *
	 */
	@NonNull
	public Optional<Method> getOptionalMethod(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getMethods0(clazz, includeSuper, condition).findFirst();
	}

	/**
	 * Get optional with first method from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Optional with method by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Optional<Method> getOptionalMethod(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getOptionalMethod(clazz, ReflectionUtils.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get optional with first method from specified class by condition (may include super class methods)
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Optional with method by condition
	 *
	 */
	@NonNull
	public Optional<Method> getOptionalMethod(@NonNull Class<?> clazz, boolean includeSuper, @NonNull MethodCondition condition) {
		return ReflectionUtils.getOptionalMethod(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get optional with first method from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Optional with method by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Optional<Method> getOptionalMethod(@NonNull Class<?> clazz, @NonNull MethodCondition condition) {
		return ReflectionUtils.getOptionalMethod(clazz, ReflectionUtils.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get first method from specified class by condition (may include super class methods)
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Method by condition (or null)
	 *
	 */
	public Method getMethod(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getOptionalMethod(clazz, includeSuper, condition).orElse(null);
	}

	/**
	 * Get first method from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Method by condition (or null)
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	public Method getMethod(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getOptionalMethod(clazz, condition).orElse(null);
	}

	/**
	 * Get first method from specified class by condition (may include super class methods)
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Method by condition (or null)
	 *
	 */
	public Method getMethod(@NonNull Class<?> clazz, boolean includeSuper, @NonNull MethodCondition condition) {
		return ReflectionUtils.getMethod(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get first method from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Method by condition (or null)
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	public Method getMethod(@NonNull Class<?> clazz, @NonNull MethodCondition condition) {
		return ReflectionUtils.getMethod(clazz, ReflectionUtils.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get first method from specified class by condition (may include super class methods). If method not found, throw exception
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Method by condition
	 *
	 */
	@NonNull
	public Method getMethodOrThrow(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getOptionalMethod(clazz, includeSuper, condition).orElseThrow();
	}

	/**
	 * Get first method from specified class by condition with default `includeSuper` parameter value. If method not found, throw exception
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Method by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 */
	@NonNull
	public Method getMethodOrThrow(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return ReflectionUtils.getOptionalMethod(clazz, condition).orElseThrow();
	}

	/**
	 * Get first method from specified class by condition (may include super class methods). If method not found, throw exception
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param condition    Method condition
	 * @return Method by condition
	 *
	 */
	@NonNull
	public Method getMethodOrThrow(@NonNull Class<?> clazz, boolean includeSuper, @NonNull MethodCondition condition) {
		return ReflectionUtils.getMethodOrThrow(clazz, includeSuper, condition.asPredicate());
	}

	/**
	 * Get first method from specified class by condition with default `includeSuper` parameter value. If method not found, throw exception
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Method by condition
	 * @see ReflectionUtils#setDefaultIncludeSuperClassMethods(boolean)
	 */
	@NonNull
	public Method getMethodOrThrow(@NonNull Class<?> clazz, @NonNull MethodCondition condition) {
		return ReflectionUtils.getMethodOrThrow(clazz, ReflectionUtils.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get classes that loaded in class loader
	 *
	 * @param loader Class loader
	 * @return List of loaded classes
	 */
	@NonNull
	public List<Class<?>> getLoadedClasses(@NonNull ClassLoader loader) {
		try {
			return (List<Class<?>>) CLASSES_FIELD.get(loader);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get class source file/directory path
	 *
	 * @param clazz Class, source path of which should be returned
	 * @return Source path of specified class
	 */
	@NonNull
	@SneakyThrows
	public Path getSourcePath(@NonNull Class<?> clazz) {
		String path = clazz.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
		return Path.of(new URI(path));
	}


}
