package org.apache.bcel.util;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.bcel.classfile.JavaClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom synthetic repository, that contains loaded classes into map without soft-referencing
 *
 */
public final class CustomSyntheticRepository extends AbstractClassPathRepository {

	private static final Map<ClassPath, CustomSyntheticRepository> MAP = new ConcurrentHashMap<>();

	private final Map<String, JavaClass> loadedClasses = new HashMap<>();
	private final Map<String, JavaClass> unmodifiableLoadedClasses = Collections.unmodifiableMap(this.loadedClasses);

	private CustomSyntheticRepository(@NonNull ClassPath classPath) {
		super(classPath);
	}

	@NonNull
	public Map<String, JavaClass> getLoadedClasses() {
		return this.unmodifiableLoadedClasses;
	}

	public JavaClass findOrLoadClass(@NonNull String name) {
		JavaClass existsClass = this.findClass(name);
		if (existsClass != null) {
			return existsClass;
		}
		try {
			return this.loadClass(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public JavaClass findClass(@NonNull String className) {
		return this.loadedClasses.get(className);
	}

	@Override
	public void removeClass(@NonNull JavaClass clazz) {
		this.loadedClasses.remove(clazz.getClassName());
	}

	@Override
	public void storeClass(@NonNull JavaClass clazz) {
		this.loadedClasses.put(clazz.getClassName(), clazz);
		clazz.setRepository(this);
	}

	@Override
	public void clear() {
		this.loadedClasses.clear();
	}

	@NonNull
	public static CustomSyntheticRepository getInstance() {
		return getInstance(ClassPath.SYSTEM_CLASS_PATH);
	}

	@NonNull
	public static CustomSyntheticRepository getInstance(final ClassPath classPath) {
		return MAP.computeIfAbsent(classPath, CustomSyntheticRepository::new);
	}

}
