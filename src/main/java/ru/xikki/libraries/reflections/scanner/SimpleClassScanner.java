package ru.xikki.libraries.reflections.scanner;

import lombok.*;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.CustomSyntheticRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import ru.xikki.libraries.reflections.ReflectionUtils;
import ru.xikki.libraries.reflections.asm.CustomClassWriter;
import ru.xikki.libraries.reflections.condition.MethodCondition;
import ru.xikki.libraries.reflections.iterator.DirectoryClassIterator;
import ru.xikki.libraries.reflections.iterator.JarClassIterator;
import ru.xikki.libraries.reflections.modifier.IClassModifier;
import ru.xikki.libraries.reflections.processor.IClassProcessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.jar.JarFile;

@Getter
@ToString
@EqualsAndHashCode
final class SimpleClassScanner implements IClassScanner {

	private final Path sourcePath;

	private final List<IClassModifier> modifiers = new LinkedList<>();
	private final List<IClassProcessor> processors = new LinkedList<>();

	private final CustomSyntheticRepository repository;

	private final Set<String> classNames = new HashSet<>();
	private final Set<String> newClassNames = new HashSet<>();

	SimpleClassScanner(@NonNull Path path) {
		this.sourcePath = path;
		JarFile currentJarFile = null;
		try (ClassPath classPath = new ClassPath(CustomSyntheticRepository.getInstance().getClassPath(), path.toString())) {
			this.repository = CustomSyntheticRepository.getInstance(classPath);

			Iterator<String> classIterator;
			if (Files.isDirectory(path)) {
				classIterator = new DirectoryClassIterator(path);
			} else {
				currentJarFile = new JarFile(path.toFile());
				classIterator = new JarClassIterator(currentJarFile);
			}

			while (classIterator.hasNext()) {
				String className = classIterator.next();
				this.classNames.add(className);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			if (currentJarFile != null) {
				currentJarFile.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@NonNull
	@Override
	public Set<String> getClassNames() {
		return Collections.unmodifiableSet(this.classNames);
	}

	@Override
	public JavaClass getClassByName(@NonNull String name) {
		return this.repository.findOrLoadClass(name);
	}

	@Override
	public void registerClass(@NonNull JavaClass javaClass) {
		this.classNames.add(javaClass.getClassName());
		this.repository.storeClass(javaClass);
		this.newClassNames.add(javaClass.getClassName());
	}

	@Override
	public void unregisterClass(@NonNull JavaClass javaClass) {
		throw new NotImplementedException();
	}

	@NonNull
	@Override
	public List<IClassModifier> getModifiers() {
		return Collections.unmodifiableList(this.modifiers);
	}

	@NonNull
	@Override
	public IClassScanner registerModifier(@NonNull IClassModifier modifier) {
		this.modifiers.add(modifier);
		return this;
	}

	@NonNull
	@Override
	public IClassScanner unregisterModifier(@NonNull IClassModifier modifier) {
		this.modifiers.remove(modifier);
		return this;
	}

	@NonNull
	@Override
	public IClassScanner unregisterAllModifiers() {
		List.copyOf(this.modifiers)
				.forEach(this::unregisterModifier);
		return this;
	}

	@SneakyThrows
	public IClassScanner dump(@NonNull ClassLoader loader) {
		ReflectionUtils.setClassAccessible(ClassLoader.class);
		Method method = ReflectionUtils.getMethodOrThrow(
				ClassLoader.class,
				MethodCondition.create()
						.withName("defineClass1")
						.withStatic(true)
						.withReturnType(Class.class)
						.withParameters(
								ClassLoader.class,
								String.class,
								byte[].class,
								int.class,
								int.class,
								ProtectionDomain.class,
								String.class
						)
		);
		method.setAccessible(true);
		ReflectionUtils.resetClassAccessible(ClassLoader.class);
		this.newClassNames.stream()
				.map(this.repository::findClass)
				.filter(Objects::nonNull)
				.forEach((javaClass) -> {
					byte[] bytes = javaClass.getBytes();
					ClassReader reader = new ClassReader(bytes);
					CustomClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, loader);
					reader.accept(writer, 0);
					bytes = writer.toByteArray();
					try {
						method.invoke(
								loader,
								loader,
								javaClass.getClassName(),
								bytes,
								0,
								bytes.length,
								this.getClass().getProtectionDomain(),
								null
						);
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				});
		return this;
	}

	@NonNull
	@Override
	public List<IClassProcessor> getProcessors() {
		return Collections.unmodifiableList(this.processors);
	}

	@Override
	public IClassScanner registerProcessor(@NonNull IClassProcessor processor) {
		this.processors.add(processor);
		return this;
	}

	@Override
	public IClassScanner unregisterProcessor(@NonNull IClassProcessor processor) {
		this.processors.remove(processor);
		return this;
	}

	@Override
	public IClassScanner unregisterAllProcessors() {
		List.copyOf(this.processors)
				.forEach(this::unregisterProcessor);
		return this;
	}

}
