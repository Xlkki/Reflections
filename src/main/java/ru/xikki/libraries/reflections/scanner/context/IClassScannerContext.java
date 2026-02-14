package ru.xikki.libraries.reflections.scanner.context;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public interface IClassScannerContext {

	@NonNull
	Map<String, List<Object>> getEntities();

	@NonNull
	default List<Object> getEntities(@NonNull String name) {
		return Collections.unmodifiableList(this.getEntities().getOrDefault(name, Collections.emptyList()));
	}

	default <E> E getEntity(@NonNull String name, @NonNull Class<?> type) {
		return (E) this.getEntities(name)
				.stream()
				.filter(type::isInstance)
				.findFirst()
				.orElseGet(() -> {
					return this.getEntities().entrySet()
							.stream()
							.map(Map.Entry::getValue)
							.flatMap(List::stream)
							.filter(type::isInstance)
							.findFirst()
							.orElse(null);
				});
	}

	@NonNull
	IClassScannerContext withEntity(@NonNull String name, @NonNull Object entity);

	@NonNull
	IClassScannerContext withoutEntity(@NonNull String name, @NonNull Object entity);

	@NonNull
	IClassScannerContext withoutEntity(@NonNull Object entity);

	@NonNull
	IClassScannerContext withoutEntities(@NonNull String name);

	@NonNull
	@SneakyThrows
	default <E> E createInstance(@NonNull Constructor<E> constructor) {
		return constructor.newInstance(
				Arrays.stream(constructor.getParameters())
						.map((parameter) -> this.getEntity(parameter.getName(), parameter.getType()))
						.toArray()
		);
	}

	@SneakyThrows
	default <E> E createInstance(@NonNull Class<E> type) {
		return (E) Arrays.stream(type.getDeclaredConstructors())
				.peek((constructor) -> constructor.setAccessible(true))
				.map((constructor) -> {
					try {
						return this.createInstance(constructor);
					} catch (Throwable e) {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	@SneakyThrows
	default Object call(Object instance, @NonNull Method method) {
		if (instance == null && !Modifier.isStatic(method.getModifiers())) {
			throw new IllegalArgumentException("Instance can not be nullable for non-static methods");
		}
		return method.invoke(
				instance,
				Arrays.stream(method.getParameters())
						.map((parameter) -> this.getEntity(parameter.getName(), parameter.getType()))
						.toArray()
		);
	}

	default Object call(@NonNull Method method) {
		return this.call(null, method);
	}

	@NonNull
	static IClassScannerContext create(@NonNull Map<String, List<Object>> entities) {
		return ClassScannerContextProvider.getFactory().create(entities);
	}

	@NonNull
	static IClassScannerContext create() {
		return IClassScannerContext.create(Collections.emptyMap());
	}

	interface Factory {

		@NonNull
		IClassScannerContext create(@NonNull Map<String, List<Object>> entities);

	}

}
