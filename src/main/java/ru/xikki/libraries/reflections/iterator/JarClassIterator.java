package ru.xikki.libraries.reflections.iterator;

import lombok.NonNull;
import ru.xikki.libraries.reflections.ReflectionUtils;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Jar file class iterator for class scanner
 */
public final class JarClassIterator implements Iterator<String> {

	private final Enumeration<JarEntry> entries;
	private String nextElement;

	public JarClassIterator(@NonNull JarFile jarFile) {
		this.entries = jarFile.entries();
		this.next();
	}

	@Override
	public boolean hasNext() {
		return this.nextElement != null;
	}

	@NonNull
	@Override
	public String next() {
		String currentElement = this.nextElement;
		this.nextElement = null;
		while (this.entries.hasMoreElements()) {
			JarEntry entry = this.entries.nextElement();
			if (!ReflectionUtils.isClassEntry(entry)) {
				continue;
			}
			this.nextElement = ReflectionUtils.toClassName(entry);
			break;
		}

		if (currentElement == null && this.nextElement == null) {
			throw new NoSuchElementException();
		}
		return currentElement;
	}

}
