package ru.xikki.libraries.reflections.scanner.context;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

public interface IClassScannerContext {

	String CLASS_NAME_FILTER_ENTITY_NAME = "classNameFilter";
	String HOLDER_ENTITY_NAME = "holder";

	/**
	 * Get entities of class scanner context as map
	 *
	 */
	@NonNull
	Map<String, List<Object>> getEntities();

	/**
	 * Get entities by name
	 *
	 * @param name Entity name
	 * @return Entities with specified name
	 *
	 */
	@NonNull
	default List<Object> getEntities(@NonNull String name) {
		return Collections.unmodifiableList(this.getEntities().getOrDefault(name, Collections.emptyList()));
	}

	/**
	 * Get entity by type and name
	 *
	 * @param name Entity name
	 * @param type Entity type
	 * @return Entity with specified type and name (or only type if entity with specified name not found)
	 *
	 */
	default <E> E getEntity(@NonNull String name, @NonNull Class<?> type) {
		return (E) this.getEntities(name)
				.stream()
				.filter(type::isInstance)
				.findFirst()
				.orElseGet(() -> {
					return type == Object.class ? null : this.getEntities().entrySet()
							.stream()
							.map(Map.Entry::getValue)
							.flatMap(List::stream)
							.filter(type::isInstance)
							.findFirst()
							.orElse(null);
				});
	}

	default Predicate<String> getClassNameFilter() {
		return this.getEntities(CLASS_NAME_FILTER_ENTITY_NAME)
				.stream()
				.filter(Predicate.class::isInstance)
				.findFirst()
				.map(Predicate.class::cast)
				.orElse(null);
	}

	/**
	 * Add entity to context
	 *
	 * @param name   Entity name
	 * @param entity Entity
	 *
	 */
	@NonNull
	IClassScannerContext withEntity(@NonNull String name, @NonNull Object entity);

	/**
	 * Add class name filter to context (used for processors and modifiers)
	 *
	 * @param classNameFilter Class name filter
	 *
	 */
	@NonNull
	default IClassScannerContext withClassNameFilter(@NonNull Predicate<String> classNameFilter) {
		return this.withEntity(CLASS_NAME_FILTER_ENTITY_NAME, classNameFilter);
	}

	/**
	 * Add holder to context
	 *
	 * @param holder Holder object
	 *
	 */
	@NonNull
	default IClassScannerContext withHolder(@NonNull Object holder) {
		return this.withEntity(HOLDER_ENTITY_NAME, holder);
	}

	/**
	 * Remove entity from context by name and entity
	 *
	 * @param name   Entity name
	 * @param entity Entity
	 *
	 */
	@NonNull
	IClassScannerContext withoutEntity(@NonNull String name, @NonNull Object entity);

	/**
	 * Remove entity from context
	 *
	 * @param entity Entity
	 *
	 */
	@NonNull
	IClassScannerContext withoutEntity(@NonNull Object entity);

	/**
	 * Remove entities from context by name
	 *
	 * @param name Entity name
	 *
	 */
	@NonNull
	IClassScannerContext withoutEntities(@NonNull String name);

	/**
	 * Try to create object instance by context entities and constructor
	 *
	 * @param constructor Object constructor
	 * @return Object instance
	 *
	 */
	@NonNull
	@SneakyThrows
	default <E> E createInstance(@NonNull Constructor<E> constructor) {
		return constructor.newInstance(
				Arrays.stream(constructor.getParameters())
						.map((parameter) -> this.getEntity(parameter.getName(), parameter.getType()))
						.toArray()
		);
	}

	/**
	 * Try to create object instance by context entities (with used class constructor)
	 *
	 * @param type Class to create instance
	 * @return Specified class instance or null (may produce exceptions)
	 *
	 */
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

	/**
	 * Try to call method by context entities
	 *
	 * @param instance Method declared class instance (null for static methods)
	 * @param method   Method
	 * @return Object returned by method calling
	 *
	 */
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

	/**
	 * Try to call static method by context entities
	 *
	 * @param method Method (static only)
	 * @return Object returned by method calling
	 *
	 */
	default Object call(@NonNull Method method) {
		return this.call(null, method);
	}

	/**
	 * Create new class scanner context by default factory
	 *
	 * @param entities Default entities
	 * @return Class scanner context
	 * @see ClassScannerContextProvider#getFactory()
	 * @see ClassScannerContextProvider#setFactory(Factory)
	 *
	 */
	@NonNull
	static IClassScannerContext create(@NonNull Map<String, List<Object>> entities) {
		return ClassScannerContextProvider.getFactory().create(entities);
	}

	/**
	 * Create empty class scanner context by default factory
	 *
	 * @return Empty class scanner context
	 * @see ClassScannerContextProvider#getFactory()
	 * @see ClassScannerContextProvider#setFactory(Factory)
	 *
	 */
	@NonNull
	static IClassScannerContext create() {
		return IClassScannerContext.create(Collections.emptyMap());
	}

	/**
	 * Factory for IClassScannerContext
	 *
	 */
	interface Factory {

		@NonNull
		IClassScannerContext create(@NonNull Map<String, List<Object>> entities);

	}

}
