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
	private boolean defaultIncludeSuperClassMethods = false;

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
		Reflections.defaultIncludeSuperClassMethods = defaultIncludeSuperClassMethods;
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
	 * @return Value of static field (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Field field) {
		Reflections.initClass(field.getDeclaringClass());
		Object base = UNSAFE.staticFieldBase(field);
		long offset = Reflections.getStaticFieldOffset(field);
		return Reflections.getFieldValue(field, base, offset);
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
		return Optional.ofNullable(Reflections.getFieldValue(field));
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
		return Reflections.getOptionalFieldValue(field)
				.orElseThrow();
	}

	/**
	 * Get static field value
	 *
	 * @param clazz Field declared class
	 * @param name  Field name
	 * @return Value of static field (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getFieldValue(Reflections.getFieldOrThrow(clazz, name));
	}

	/**
	 * Get static field value wrapped in optional
	 *
	 * @param clazz Field declared class
	 * @param name  Field name
	 * @return Optional with value of static field
	 *
	 */
	@NonNull
	public Optional<Object> getOptionalFieldValue(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getOptionalFieldValue(Reflections.getFieldOrThrow(clazz, name));
	}

	/**
	 * Get static field value. If value is null - throw exception
	 *
	 * @param clazz Field declared class
	 * @param name  Field name
	 * @return Value of static field
	 *
	 */
	@NonNull
	public Object getFieldValueOrThrow(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getFieldValueOrThrow(Reflections.getFieldOrThrow(clazz, name));
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
		Reflections.initClass(field.getDeclaringClass());
		long offset = Reflections.getObjectFieldOffset(field);
		return Reflections.getFieldValue(field, instance, offset);
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
		return Optional.ofNullable(Reflections.getFieldValue(instance, field));
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
		return Reflections.getOptionalFieldValue(instance, field)
				.orElseThrow();
	}

	/**
	 * Get non-static field value
	 *
	 * @param instance Instance of field declared class
	 * @param name     Field name
	 * @return Value of non-static field (maybe null)
	 *
	 */
	public Object getFieldValue(@NonNull Object instance, @NonNull String name) {
		return Reflections.getFieldValue(instance, Reflections.getFieldOrThrow(instance.getClass(), name));
	}

	/**
	 * Get non-static field value wrapped in optional
	 *
	 * @param instance Instance of field declared class
	 * @param name     Field name
	 * @return Optional with value of non-static field
	 *
	 */
	@NonNull
	public Optional<Object> getOptionalFieldValue(@NonNull Object instance, @NonNull String name) {
		return Reflections.getOptionalFieldValue(instance, Reflections.getFieldOrThrow(instance.getClass(), name));
	}

	/**
	 * Get non-static field value. If value is null - throw exception
	 *
	 * @param instance Instance of field declared class
	 * @param name     Field name
	 * @return Value of non-static field
	 *
	 */
	@NonNull
	public Object getFieldValueOrThrow(@NonNull Object instance, @NonNull String name) {
		return Reflections.getFieldValueOrThrow(instance, Reflections.getFieldOrThrow(instance.getClass(), name));
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
		Reflections.initClass(field.getDeclaringClass());
		Object base = UNSAFE.staticFieldBase(field);
		long offset = Reflections.getStaticFieldOffset(field);
		Reflections.setFieldValue(field, base, offset, value);
	}

	/**
	 * Set static field value
	 *
	 * @param clazz Field declared class
	 * @param name  Field name
	 * @param value New field value (maybe null)
	 *
	 */
	public void setFieldValue(@NonNull Class<?> clazz, @NonNull String name, Object value) {
		Reflections.setFieldValue(Reflections.getFieldOrThrow(clazz, name), value);
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

	/**
	 * Set non-static field value
	 *
	 * @param instance Instance of field declared class
	 * @param name     Field name
	 * @param value    New field value (maybe null)
	 *
	 *
	 */
	public void setFieldValue(@NonNull Object instance, @NonNull String name, Object value) {
		Reflections.setFieldValue(instance, Reflections.getFieldOrThrow(instance.getClass(), name), value);
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
	 * Get specified class fields by name (may include super class fields)
	 *
	 * @param clazz        Class fields of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param name         Field name
	 * @return Array with fields by name
	 *
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getFields(clazz, includeSuper, (field) -> field.getName().equals(name));
	}

	/**
	 * Get specified class fields by name with default `includeSuper` parameter value
	 *
	 * @param clazz Class fields of which should be returned
	 * @param name  Field name
	 * @return Array with fields by name
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Field[] getFields(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getFields(clazz, Reflections.getDefaultIncludeSuperClassFields(), name);
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
	 * Get optional with first field from specified class by name (may include super class fields)
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param name         Field name
	 * @return Optional with field by name
	 *
	 */
	@NonNull
	public Optional<Field> getOptionalField(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getOptionalField(clazz, includeSuper, (field) -> field.getName().equals(name));
	}

	/**
	 * Get optional with first field from specified class by name with default `includeSuper` parameter value
	 *
	 * @param clazz Class field of which should be returned
	 * @param name  Field name
	 * @return Optional with field by name
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	@NonNull
	public Optional<Field> getOptionalField(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getOptionalField(clazz, Reflections.getDefaultIncludeSuperClassFields(), name);
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
	 * Get first field from specified class by name (may include super class fields)
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param name         Field name
	 * @return Field by name (or null)
	 *
	 */
	public Field getField(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getField(clazz, includeSuper, (field) -> field.getName().equals(name));
	}

	/**
	 * Get first field from specified class by name with default `includeSuper` parameter value
	 *
	 * @param clazz Class field of which should be returned
	 * @param name  Field name
	 * @return Field by name (or null)
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 *
	 */
	public Field getField(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getField(clazz, Reflections.getDefaultIncludeSuperClassFields(), name);
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

	/**
	 * Get first field from specified class by name (may include super class fields). If field not found, throw exception
	 *
	 * @param clazz        Class field of which should be returned
	 * @param includeSuper true - if result should contain super class fields, otherwise - false
	 * @param name         Field name
	 * @return Field by name
	 *
	 */
	@NonNull
	public Field getFieldOrThrow(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getFieldOrThrow(clazz, includeSuper, (field) -> field.getName().equals(name));
	}

	/**
	 * Get first field from specified class by name with default `includeSuper` parameter value. If field not found, throw exception
	 *
	 * @param clazz Class field of which should be returned
	 * @param name  Field name
	 * @return Field by name
	 * @see Reflections#setDefaultIncludeSuperClassFields(boolean)
	 */
	@NonNull
	public Field getFieldOrThrow(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getFieldOrThrow(clazz, Reflections.getDefaultIncludeSuperClassFields(), name);
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
			Method[] superClassMethods = Reflections.getMethods(superClass, true);
			return Stream.concat(Arrays.stream(currentClassMethods), Arrays.stream(superClassMethods)).toArray(Method[]::new);
		}
	}

	/**
	 * Get specified class methods with default `includeSuper` parameter value
	 *
	 * @param clazz Class methods of which should be returned
	 * @return Array with methods
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz) {
		return Reflections.getMethods(clazz, Reflections.getDefaultIncludeSuperClassMethods());
	}

	@NonNull
	private Stream<Method> getMethods0(@NonNull Class<?> clazz, boolean includeSuper, @NonNull Predicate<Method> condition) {
		return Arrays.stream(Reflections.getMethods(clazz, includeSuper)).filter(condition);
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
		return Reflections.getMethods0(clazz, includeSuper, condition).toArray(Method[]::new);
	}

	/**
	 * Get specified class methods by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class methods of which should be returned
	 * @param condition Method condition
	 * @return Array with methods
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return Reflections.getMethods(clazz, Reflections.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get specified class methods by name (may include super class methods)
	 *
	 * @param clazz        Class methods of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param name         Method name
	 * @return Array with methods by name
	 *
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getMethods(clazz, includeSuper, (method) -> method.getName().equals(name));
	}

	/**
	 * Get specified class methods by name with default `includeSuper` parameter value
	 *
	 * @param clazz Class methods of which should be returned
	 * @param name  Method name
	 * @return Array with methods by name
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Method[] getMethods(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getMethods(clazz, Reflections.getDefaultIncludeSuperClassMethods(), name);
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
		return Reflections.getMethods0(clazz, includeSuper, condition).findFirst();
	}

	/**
	 * Get optional with first method from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Optional with method by condition
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Optional<Method> getOptionalMethod(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return Reflections.getOptionalMethod(clazz, Reflections.getDefaultIncludeSuperClassMethods(), condition);
	}

	/**
	 * Get optional with first method from specified class by name (may include super class methods)
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param name         Method name
	 * @return Optional with method by name
	 *
	 */
	@NonNull
	public Optional<Method> getOptionalMethod(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getOptionalMethod(clazz, includeSuper, (method) -> method.getName().equals(name));
	}

	/**
	 * Get optional with first method from specified class by name with default `includeSuper` parameter value
	 *
	 * @param clazz Class method of which should be returned
	 * @param name  Method name
	 * @return Optional with method by name
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	@NonNull
	public Optional<Method> getOptionalMethod(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getOptionalMethod(clazz, Reflections.getDefaultIncludeSuperClassMethods(), name);
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
		return Reflections.getOptionalMethod(clazz, includeSuper, condition).orElse(null);
	}

	/**
	 * Get first method from specified class by condition with default `includeSuper` parameter value
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Method by condition (or null)
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	public Method getMethod(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return Reflections.getOptionalMethod(clazz, condition).orElse(null);
	}

	/**
	 * Get first method from specified class by name (may include super class methods)
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param name         Method name
	 * @return Method by name (or null)
	 *
	 */
	public Method getMethod(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getMethod(clazz, includeSuper, (method) -> method.getName().equals(name));
	}

	/**
	 * Get first method from specified class by name with default `includeSuper` parameter value
	 *
	 * @param clazz Class method of which should be returned
	 * @param name  Method name
	 * @return Method by name (or null)
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 *
	 */
	public Method getMethod(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getMethod(clazz, Reflections.getDefaultIncludeSuperClassMethods(), name);
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
		return Reflections.getOptionalMethod(clazz, includeSuper, condition).orElseThrow();
	}

	/**
	 * Get first method from specified class by condition with default `includeSuper` parameter value. If method not found, throw exception
	 *
	 * @param clazz     Class method of which should be returned
	 * @param condition Method condition
	 * @return Method by condition
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 */
	@NonNull
	public Method getMethodOrThrow(@NonNull Class<?> clazz, @NonNull Predicate<Method> condition) {
		return Reflections.getOptionalMethod(clazz, condition).orElseThrow();
	}

	/**
	 * Get first method from specified class by name (may include super class methods). If method not found, throw exception
	 *
	 * @param clazz        Class method of which should be returned
	 * @param includeSuper true - if result should contain super class methods, otherwise - false
	 * @param name         Method name
	 * @return Method by name
	 *
	 */
	@NonNull
	public Method getMethodOrThrow(@NonNull Class<?> clazz, boolean includeSuper, @NonNull String name) {
		return Reflections.getMethodOrThrow(clazz, includeSuper, (method) -> method.getName().equals(name));
	}

	/**
	 * Get first method from specified class by name with default `includeSuper` parameter value. If method not found, throw exception
	 *
	 * @param clazz Class method of which should be returned
	 * @param name  Method name
	 * @return Method by name
	 * @see Reflections#setDefaultIncludeSuperClassMethods(boolean)
	 */
	@NonNull
	public Method getMethodOrThrow(@NonNull Class<?> clazz, @NonNull String name) {
		return Reflections.getMethodOrThrow(clazz, Reflections.getDefaultIncludeSuperClassMethods(), name);
	}

}
