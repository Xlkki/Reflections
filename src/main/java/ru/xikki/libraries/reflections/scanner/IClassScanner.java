package ru.xikki.libraries.reflections.scanner;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import ru.xikki.libraries.reflections.ReflectionUtils;
import ru.xikki.libraries.reflections.bcel.BCELUtils;
import ru.xikki.libraries.reflections.modifier.ClassModifier;
import ru.xikki.libraries.reflections.modifier.IClassModifier;
import ru.xikki.libraries.reflections.processor.ClassProcessor;
import ru.xikki.libraries.reflections.processor.IClassProcessor;
import ru.xikki.libraries.reflections.processor.ProcessorOrder;
import ru.xikki.libraries.reflections.scanner.context.IClassScannerContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface IClassScanner extends AutoCloseable, IClassModifier.Holder, IClassProcessor.Holder {

	/**
	 * Get source file (or directory) path
	 */
	@NonNull
	Path getSourcePath();

	/**
	 * Get class names
	 *
	 * @return Set with class names
	 */
	@NonNull
	Set<String> getClassNames();

	/**
	 * Get class by name
	 *
	 * @param name Name of class that should be returned
	 * @return Class by specified name or null
	 */
	JavaClass getClassByName(@NonNull String name);

	/**
	 * Get classes by name and class conditions
	 *
	 * @param nameCondition  Condition for class name
	 * @param classCondition Condition for class
	 * @return List with matched classes (may be empty)
	 */
	@NonNull
	default List<JavaClass> getClassesByCondition(Predicate<String> nameCondition, @NonNull Predicate<JavaClass> classCondition) {
		return this.getClassNames()
				.stream()
				.filter((className) -> nameCondition == null || nameCondition.test(className))
				.map(this::getClassByName)
				.filter(Objects::nonNull)
				.filter(classCondition)
				.toList();
	}

	/**
	 * Get classes by name and class conditions
	 *
	 * @param classCondition Condition for class
	 * @return List with matched classes (may be empty)
	 */
	@NonNull
	default List<JavaClass> getClassesByCondition(@NonNull Predicate<JavaClass> classCondition) {
		return this.getClassesByCondition(null, classCondition);
	}

	/**
	 * Register new class in class loader. That class may be processed by <code>IClassProcessor</code> or modified
	 * by <code>IClassModifier</code> and inject into ClassLoader by calling <code>dump</code> method
	 *
	 * @param javaClass Class that should be registered
	 */
	void registerClass(@NonNull JavaClass javaClass);

	/**
	 * Unregister class from class loader. That class may not be processed by <code>IClassProcessor</code> or modified
	 * by <code>IClassModifier</code>
	 */
	void unregisterClass(@NonNull JavaClass javaClass);

	/**
	 * Find and create instance of class modifiers
	 *
	 * @param context Context for instance creating
	 * @param loader  Class loader in which classes should be searched
	 * @return List with class modifiers (may be empty)
	 */
	@NonNull
	default List<IClassModifier> findModifiers(@NonNull IClassScannerContext context, @NonNull ClassLoader loader) {
		return this.getClassesByCondition(
						context.getClassNameFilter(), (javaClass) -> {
							return BCELUtils.hasAnnotation(javaClass, ClassModifier.class);
						}
				)
				.stream()
				.map((javaClass) -> BCELUtils.getClass(loader, javaClass))
				.map((clazz) -> {
					Object instance = context.createInstance(clazz);
					if (instance == null) {
						throw new IllegalArgumentException("Can not create instance of %s".formatted(clazz.getSimpleName()));
					}
					return instance;
				})
				.peek((instance) -> {
					if (!(instance instanceof IClassModifier)) {
						throw new IllegalStateException("Class %s should be implement %s interface".formatted(instance.getClass().getSimpleName(), IClassModifier.class.getSimpleName()));
					}
				})
				.map(IClassModifier.class::cast)
				.toList();
	}

	/**
	 * Find and create instance of class processors
	 *
	 * @param context Context for instance creating
	 * @param order   Class processor applying order
	 * @param loader  Class loader in which classes should be searched
	 * @return List with class processors (may be empty)
	 */
	@NonNull
	default List<IClassProcessor> findProcessors(@NonNull IClassScannerContext context, @NonNull ProcessorOrder order, @NonNull ClassLoader loader) {
		return this.getClassesByCondition(
						context.getClassNameFilter(), (javaClass) -> {
							if (!BCELUtils.hasAnnotation(javaClass, ClassProcessor.class)) {
								return false;
							}
							AnnotationEntry entry = BCELUtils.getAnnotation(javaClass, ClassProcessor.class);
							ProcessorOrder entryOrder = BCELUtils.getOptionalFieldValue(entry, "value")
									.map(ProcessorOrder::valueOf)
									.orElse(ProcessorOrder.NORMAL);
							return entryOrder.equals(order);
						}
				)
				.stream()
				.map((javaClass) -> BCELUtils.getClass(loader, javaClass))
				.map((clazz) -> {
					Object instance = context.createInstance(clazz);
					if (instance == null) {
						throw new IllegalArgumentException("Can not create instance of %s".formatted(clazz.getSimpleName()));
					}
					return instance;
				})
				.peek((instance) -> {
					if (!(instance instanceof IClassProcessor)) {
						throw new IllegalStateException("Class %s should be implement %s interface".formatted(instance.getClass().getSimpleName(), IClassProcessor.class.getSimpleName()));
					}
				})
				.map(IClassProcessor.class::cast)
				.toList();
	}

	/**
	 * Call registered class modifiers for all classes
	 */
	@NonNull
	@SneakyThrows
	default IClassScanner modifyClasses() {
		Set.copyOf(this.getClassNames()).forEach((className) -> {
			List<IClassModifier> appliedModifiers = this.getModifiers().stream()
					.filter((modifier) -> modifier.shouldModify(this, className))
					.toList();
			if (appliedModifiers.isEmpty()) {
				return;
			}
			JavaClass javaClass = this.getClassByName(className);
			if (javaClass == null) {
				return;
			}
			appliedModifiers = appliedModifiers.stream()
					.filter((modifier) -> modifier.shouldModify(this, javaClass))
					.toList();
			if (appliedModifiers.isEmpty()) {
				return;
			}
			try {
				ClassGen classGenerator = new ClassGen(javaClass);
				appliedModifiers.forEach((modifier) -> modifier.modifyClass(this, classGenerator));
				this.registerClass(classGenerator.getJavaClass());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return this;
	}

	/**
	 * Dump all modified classes into class loader
	 *
	 * @param loader Class loader in which modified classes should be injected
	 */
	@NonNull
	IClassScanner dump(@NonNull ClassLoader loader);

	/**
	 * Call registered class processors for all classes
	 *
	 * @param context Context for instance creating
	 * @param loader  Loader by which processed classes should be loaded
	 */
	@SneakyThrows
	default void processClasses(@NonNull IClassScannerContext context, @NonNull ClassLoader loader) {
		this.getClassNames().forEach((className) -> {
			List<IClassProcessor> appliedProcessors = this.getProcessors().stream()
					.filter((processor) -> processor.shouldProcess(this, className))
					.toList();
			if (appliedProcessors.isEmpty()) {
				return;
			}
			JavaClass javaClass = this.getClassByName(className);
			if (javaClass == null) {
				return;
			}
			appliedProcessors = appliedProcessors.stream()
					.filter((processor) -> processor.shouldProcess(this, javaClass))
					.toList();
			if (appliedProcessors.isEmpty()) {
				return;
			}
			appliedProcessors.forEach((processor) -> processor.processClass(this, javaClass));
			Set<IClassProcessor> loadedClassProcessors = appliedProcessors.stream()
					.filter((processor) -> processor.shouldLoadClass(this, javaClass))
					.collect(Collectors.toUnmodifiableSet());
			Set<IClassProcessor> instanceProcessors = appliedProcessors.stream()
					.filter((processor) -> processor.shouldCreateInstance(this, javaClass))
					.collect(Collectors.toUnmodifiableSet());
			if (!loadedClassProcessors.isEmpty() || !instanceProcessors.isEmpty()) {
				Class<?> clazz = BCELUtils.getClass(loader, javaClass);
				try {
					loadedClassProcessors.forEach((processor) -> processor.processClass(this, clazz));
					if (!instanceProcessors.isEmpty()) {
						Object instance = context.createInstance(clazz);
						if (instance == null) {
							throw new IllegalArgumentException("Can not create instance of class %s".formatted(clazz.getName()));
						}
						instanceProcessors.forEach((processor) -> processor.processInstance(this, instance));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Close class scanner
	 */
	default void close() {
		ClassScannerProvider.SCANNERS.remove(this.getSourcePath());
	}

	/**
	 * Get or create class scanner by source path
	 *
	 * @param path Source path
	 * @return Class scanner by specified source path
	 */
	@NonNull
	static IClassScanner of(@NonNull Path path) {
		return ClassScannerProvider.getOrCreateScanner(path);
	}

	/**
	 * Get or create class scanner by class source path
	 *
	 * @param clazz Class by source path of which scanner should be created
	 * @return Class scanner by specified source
	 */
	@NonNull
	static IClassScanner of(@NonNull Class<?> clazz) {
		return IClassScanner.of(ReflectionUtils.getSourcePath(clazz));
	}

	/**
	 * Get or create class scanner of caller class
	 *
	 * @return Class scanner of caller class
	 */
	@NonNull
	static IClassScanner of() {
		return IClassScanner.of(ReflectionUtils.getCallerClass());
	}

	interface Factory {

		@NonNull
		IClassScanner create(@NonNull Path path);

	}

}
