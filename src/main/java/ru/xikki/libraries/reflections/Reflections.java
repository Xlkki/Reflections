package ru.xikki.libraries.reflections;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@UtilityClass
public class Reflections {

	private final Unsafe UNSAFE;

	private final Field MODULE_FIELD;
	private final Method GET_DECLARED_FIELDS_METHOD;
	private final Method GET_DECLARED_METHODS_METHOD;

	private final Map<Class<?>, Module> DEFAULT_MODULES;

	private boolean defaultIncludeSuperClassFields = false;

	static {
		try {
			DEFAULT_MODULES = new HashMap<>();

			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);

			MODULE_FIELD = Class.class.getDeclaredField("module");

			Reflections.setClassAccessible(Class.class);
			GET_DECLARED_FIELDS_METHOD = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
			GET_DECLARED_METHODS_METHOD = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
			GET_DECLARED_FIELDS_METHOD.setAccessible(true);
			GET_DECLARED_METHODS_METHOD.setAccessible(true);
			Reflections.resetClassAccessible(Class.class);
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
		Reflections.defaultIncludeSuperClassFields = defaultIncludeSuperClassFields;
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

	private long getObjectFieldOffset(@NonNull Field field) {
		if (!field.getDeclaringClass().isRecord()) {
			return UNSAFE.objectFieldOffset(field);
		} else {
			throw new IllegalStateException("Temporary not supported for record classes");
		}
	}

	private long getStaticFieldOffset(@NonNull Field field) {
		if (!field.getDeclaringClass().isRecord()) {
			return UNSAFE.staticFieldOffset(field);
		} else {
			throw new IllegalStateException("Temporary not supported for record classes");
		}
	}

	/**
	 * Get static field value
	 *
	 * @param field Field value of which should be returned
	 * @return Value of static method (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Field field) {
		Reflections.initClass(field.getDeclaringClass());
		Object base = UNSAFE.staticFieldBase(field);
		long offset = Reflections.getStaticFieldOffset(field);
		return Reflections.getFieldValue(field, base, offset);
	}

	//TODO add methods with optional and non-null

	//TODO add methods with field name

	/**
	 * Get non-static field value
	 *
	 * @param instance Instance of field declared class
	 * @param field    Field value of which should be returned
	 * @return Value of non-static method (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Object instance, @NonNull Field field) {
		Reflections.initClass(field.getDeclaringClass());
		long offset = Reflections.getObjectFieldOffset(field);
		return Reflections.getFieldValue(field, instance, offset);
	}

	//TODO add methods with optional and non-null

	//TODO add methods with field name

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
	 *
	 */
	public void setFieldValue(@NonNull Field field, Object value) {
		Reflections.initClass(field.getDeclaringClass());
		Object base = UNSAFE.staticFieldBase(field);
		long offset = Reflections.getStaticFieldOffset(field);
		Reflections.setFieldValue(field, base, offset, value);
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
		Reflections.initClass(field.getDeclaringClass());
		long offset = Reflections.getObjectFieldOffset(field);
		Reflections.setFieldValue(field, instance, offset, value);
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
		Reflections.setFieldValue(clazz, MODULE_FIELD, Reflections.class.getModule());
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
		Reflections.setFieldValue(clazz, MODULE_FIELD, defaultModule);
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
			Field[] superClassFields = Reflections.getFields(superClass, true);
			return Stream.concat(Arrays.stream(currentClassFields), Arrays.stream(superClassFields)).toArray(Field[]::new);
		}
	}

	/**
	 * Get specified class fields with default `includeSuper` parameter value
	 *
	 * @param clazz Class fields of which should be returned
	 * @return Array with fields
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz) {
		return Reflections.getFields(clazz, Reflections.getDefaultIncludeSuperClassFields());
	}

	@NonNull
	private Stream<Field> getFields0(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Field> condition) {
		return Arrays.stream(Reflections.getFields(clazz, includeSuper)).filter(condition);
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
		return Reflections.getFields0(clazz, includeSuper, condition).toArray(Field[]::new);
	}

	/**
	 * Get specified class fields by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class fields of which should be returned
	 * @param condition Field condition
	 * @return Array with fields
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return Reflections.getFields(clazz, Reflections.getDefaultIncludeSuperClassFields(), condition);
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
		return Reflections.getFields0(clazz, includeSuper, condition).findFirst();
	}

	/**
	 * Get optional with first field from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Optional with field by condition
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Optional<Field> getOptionalField(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return Reflections.getOptionalField(clazz, Reflections.getDefaultIncludeSuperClassFields(), condition);
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
		return Reflections.getOptionalField(clazz, includeSuper, condition).orElse(null);
	}

	/**
	 * Get first field from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Field by condition (or null)
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	public Field getField(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return Reflections.getOptionalField(clazz, condition).orElse(null);
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
		return Reflections.getOptionalField(clazz, includeSuper, condition).orElseThrow();
	}

	/**
	 * Get first field from specified class by condition with default `includeSuper` parameter value. If field not found, throw exception
	 *
	 * @param clazz     Class field of which should be returned
	 * @param condition Field condition
	 * @return Field by condition
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 */
	@NonNull
	public Field getFieldOrThrow(@NonNull Class<?> clazz, @NonNull Predicate<Field> condition) {
		return Reflections.getOptionalField(clazz, condition).orElseThrow();
	}

}
