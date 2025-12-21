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
		return (E) UNSAFE.allocateInstance(clazz);
	}

}
