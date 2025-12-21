package ru.xikki.libraries.reflections;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@UtilityClass
public class Reflections {

	private final Unsafe UNSAFE;

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get unsafe instance
	 *
	 * @return Unsafe instance
	 * */
	@NonNull
	public Unsafe getUnsafe() {
		return UNSAFE;
	}

	/**
	 * Init class (calling it static block if class is not initialized)
	 *
	 * @param clazz Class that should be initialized
	 * */
	@SneakyThrows
	public void initClass(@NonNull Class<?> clazz) {
		Class.forName(clazz.getName(), true, clazz.getClassLoader());
	}

	/**
	 * Create empty instance of specified class
	 *
	 * @param clazz Class instance of which should be created
	 *
	 * @return Instance of specified class
	 *
	 * @throws InstantiationException If specified class is interface or abstract
	 * */
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
	 *
	 * @return Value of static method (maybe null)
	 * */
	public Object getFieldValue(@NonNull Field field) {
		Reflections.initClass(field.getDeclaringClass());
		Object base = UNSAFE.staticFieldBase(field);
		long offset = Reflections.getStaticFieldOffset(field);
		return Reflections.getFieldValue(field, base, offset);
	}

	/**
	 * Get non-static field value
	 *
	 * @param instance Instance of field declared class
	 * @param field Field value of which should be returned
	 *
	 * @return Value of non-static method (maybe null)
	 * */
	public Object getFieldValue(@NonNull Object instance, @NonNull Field field) {
		Reflections.initClass(field.getDeclaringClass());
		long offset = Reflections.getObjectFieldOffset(field);
		return Reflections.getFieldValue(field, instance, offset);
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

}
