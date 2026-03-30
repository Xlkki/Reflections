package ru.xikki.libraries.reflections.scanner;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public final class ClassScannerProvider {

	final Map<Path, IClassScanner> SCANNERS = new HashMap<>();

	@Getter
	@Setter
	@NonNull
	static IClassScanner.Factory factory = SimpleClassScanner::new;

	/**
	 * Get all registered class scanners
	 */
	@NonNull
	public Map<Path, IClassScanner> getScanners() {
		return Collections.unmodifiableMap(SCANNERS);
	}

	/**
	 * Get registered class scanner by source path
	 *
	 * @param path Source path
	 * @return Registered class scanner by specified source path or null
	 */
	public static IClassScanner getScanner(@NonNull Path path) {
		return SCANNERS.get(path);
	}

	/**
	 * Get registered class scanner by source path or create new
	 *
	 * @param path Source path
	 * @return Class scanner by specified source path
	 */
	@NonNull
	public static IClassScanner getOrCreateScanner(@NonNull Path path) {
		return SCANNERS.computeIfAbsent(path, (__) -> factory.create(path));
	}
}
