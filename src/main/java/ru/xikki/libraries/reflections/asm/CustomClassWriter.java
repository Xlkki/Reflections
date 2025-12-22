package ru.xikki.libraries.reflections.asm;

import lombok.Getter;
import lombok.NonNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Custom ASM class writer with specific class loader
 */
@Getter
public final class CustomClassWriter extends ClassWriter {

	private final ClassLoader classLoader;

	public CustomClassWriter(@NonNull ClassReader classReader, int flags, @NonNull ClassLoader classLoader) {
		super(classReader, flags);
		this.classLoader = classLoader;
	}

	public CustomClassWriter(int flags, @NonNull ClassLoader classLoader) {
		super(flags);
		this.classLoader = classLoader;
	}

}
