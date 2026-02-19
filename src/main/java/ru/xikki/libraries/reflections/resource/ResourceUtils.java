package ru.xikki.libraries.reflections.resource;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.xikki.libraries.reflections.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@UtilityClass
public final class ResourceUtils {

	/**
	 * Save resource from class loader to specified path
	 *
	 * @param loader      Source class loader
	 * @param sourcePath  Source resource path
	 * @param destinyPath Target resource path in file system
	 * @param replace     True, if exists file should be replaced by resource, otherwise false
	 * @return True, if resource saved, otherwise - false
	 */
	@SneakyThrows
	public boolean saveResource(@NonNull ClassLoader loader, @NonNull Path sourcePath, @NonNull Path destinyPath, boolean replace) {
		if (Files.exists(destinyPath)) {
			if (!replace) {
				return false;
			}
			Files.deleteIfExists(destinyPath);
		}

		Path parentPath = destinyPath.getParent();
		if (parentPath != null && !Files.exists(parentPath)) {
			Files.createDirectories(parentPath);
		}
		try (InputStream input = ResourceUtils.getResource(loader, sourcePath)) {
			if (input == null) {
				return false;
			}
			Files.copy(input, destinyPath);
		}
		return true;
	}

	/**
	 * Save resource from class loader to specified path
	 *
	 * @param loader      Source class loader
	 * @param sourcePath  Source resource path
	 * @param destinyPath Target resource path in file system
	 * @return True, if resource saved, otherwise - false
	 */
	public boolean saveResource(@NonNull ClassLoader loader, @NonNull Path sourcePath, @NonNull Path destinyPath) throws IOException {
		return ResourceUtils.saveResource(loader, sourcePath, destinyPath, false);
	}

	/**
	 * Save resource from class loader to specified path
	 *
	 * @param clazz       Class, from which classloader resource should be saved
	 * @param sourcePath  Source resource path
	 * @param destinyPath Target resource path in file system
	 * @param replace     True, if exists file should be replaced by resource, otherwise false
	 * @return True, if resource saved, otherwise - false
	 */
	public boolean saveResource(@NonNull Class<?> clazz, @NonNull Path sourcePath, @NonNull Path destinyPath, boolean replace) throws IOException {
		return ResourceUtils.saveResource(clazz.getClassLoader(), sourcePath, destinyPath, replace);
	}

	/**
	 * Save resource from class loader to specified path
	 *
	 * @param clazz       Class, from which classloader resource should be saved
	 * @param sourcePath  Source resource path
	 * @param destinyPath Target resource path in file system
	 * @return True, if resource saved, otherwise - false
	 */
	public boolean saveResource(@NonNull Class<?> clazz, @NonNull Path sourcePath, @NonNull Path destinyPath) throws IOException {
		return ResourceUtils.saveResource(clazz.getClassLoader(), sourcePath, destinyPath);
	}

	/**
	 * Save resource from class loader to specified path
	 *
	 * @param sourcePath  Source resource path
	 * @param destinyPath Target resource path in file system
	 * @param replace     True, if exists file should be replaced by resource, otherwise false
	 * @return True, if resource saved, otherwise - false
	 */
	public boolean saveResource(@NonNull Path sourcePath, @NonNull Path destinyPath, boolean replace) throws IOException {
		return ResourceUtils.saveResource(ReflectionUtils.class, sourcePath, destinyPath, replace);
	}

	/**
	 * Save resource from class loader to specified path
	 *
	 * @param sourcePath  Source resource path
	 * @param destinyPath Target resource path in file system
	 * @return True, if resource saved, otherwise - false
	 */
	public boolean saveResource(@NonNull Path sourcePath, @NonNull Path destinyPath) throws IOException {
		return ResourceUtils.saveResource(ReflectionUtils.class, sourcePath, destinyPath);
	}

	/**
	 * Get resource stream from class loader
	 *
	 * @param loader Source class loader
	 * @param path   Source resource path
	 * @return Resource stream if that exists, otherwise - null
	 */
	@SneakyThrows
	public InputStream getResource(@NonNull ClassLoader loader, @NonNull Path path) {
		URL url = loader.getResource(path.toString().replace("\\", "/"));
		if (url == null) {
			return null;
		}
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		return connection.getInputStream();
	}

	/**
	 * Get resource stream from class loader
	 *
	 * @param clazz Class, from which classloader resource should be got
	 * @param path  Source resource path
	 * @return Resource stream if that exists, otherwise - null
	 */
	public InputStream getResource(@NonNull Class<?> clazz, @NonNull Path path) {
		return ResourceUtils.getResource(clazz.getClassLoader(), path);
	}

	/**
	 * Get resource stream from class loader
	 *
	 * @param path Source resource path
	 * @return Resource stream if that exists, otherwise - null
	 */
	public InputStream getResource(@NonNull Path path) {
		return ResourceUtils.getResource(ReflectionUtils.class, path);
	}

	/**
	 * Get resources path from class loader by directory
	 *
	 * @param clazz Class, from which classloader resources should be got
	 * @param path  Resource directory path
	 * @param deep  True, if result should include subdirectories content, otherwise - false
	 * @return List with resources path
	 */
	@NonNull
	@SneakyThrows
	public List<Path> getResources(@NonNull Class<?> clazz, @NonNull Path path, boolean deep) {
		List<Path> resources = new ArrayList<>();
		URL url = clazz.getResource("/" + path.toString().replace("\\", "/"));
		if (url == null) {
			return resources;
		}
		URI uri = url.toURI();
		FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
		path = fileSystem.getPath(path.toString());

		Stream<Path> walk = Files.walk(path, 1);
		List<Path> directories = new ArrayList<>();
		Iterator<Path> iterator = walk.iterator();
		while (iterator.hasNext()) {
			Path child = iterator.next();
			if (child.toString().contains(".")) {
				resources.add(Path.of(child.toString()));
			} else if (deep) {
				if (child.equals(path)) {
					continue;
				}
				directories.add(child);
			}
		}
		fileSystem.close();
		for (Path directoryPath : directories) {
			resources.addAll(ResourceUtils.getResources(clazz, directoryPath, true));
		}
		return resources;
	}

	/**
	 * Get resources path from class loader by directory
	 *
	 * @param path Resource directory path
	 * @param deep True, if result should include subdirectories content, otherwise - false
	 * @return List with resources path
	 */
	@NonNull
	public List<Path> getResources(@NonNull Path path, boolean deep) {
		return ResourceUtils.getResources(ReflectionUtils.class, path, deep);
	}

	/**
	 * Get resources path from class loader by directory
	 *
	 * @param clazz Class, from which classloader resources should be got
	 * @param path  Resource directory path
	 * @return List with resources path
	 */
	@NonNull
	public List<Path> getResources(@NonNull Class<?> clazz, @NonNull Path path) {
		return ResourceUtils.getResources(clazz, path, false);
	}

	/**
	 * Get resources path from class loader by directory
	 *
	 * @param path Resource directory path
	 * @return List with resources path
	 */
	@NonNull
	public List<Path> getResources(@NonNull Path path) {
		return ResourceUtils.getResources(path, false);
	}

}
