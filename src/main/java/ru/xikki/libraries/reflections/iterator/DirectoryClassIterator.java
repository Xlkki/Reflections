package ru.xikki.libraries.reflections.iterator;

import lombok.NonNull;
import ru.xikki.libraries.reflections.ReflectionUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Directory class iterator for class scanner
 */
public final class DirectoryClassIterator implements Iterator<String> {

	private final Queue<String> classPaths;

	public DirectoryClassIterator(@NonNull Path rootDirectoryPath) {
		try {
			LinkedList<String> paths = new LinkedList<>();
			Files.walkFileTree(rootDirectoryPath, new SimpleFileVisitor<>() {

				@NonNull
				@Override
				public FileVisitResult visitFile(@NonNull Path filePath, @NonNull BasicFileAttributes attrs) {
					Path relativeFilePath = rootDirectoryPath.relativize(filePath);
					if (ReflectionUtils.isClassFilePath(relativeFilePath)) {
						paths.add(ReflectionUtils.toClassName(relativeFilePath));
					}
					return FileVisitResult.CONTINUE;
				}

			});
			this.classPaths = paths;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return !this.classPaths.isEmpty();
	}

	@NonNull
	@Override
	public String next() {
		if (this.classPaths.isEmpty()) {
			throw new NoSuchElementException();
		}
		return this.classPaths.poll();
	}

}
