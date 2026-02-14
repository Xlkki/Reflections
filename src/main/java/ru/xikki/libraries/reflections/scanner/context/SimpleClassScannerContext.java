package ru.xikki.libraries.reflections.scanner.context;

import lombok.NonNull;

import java.util.*;

record SimpleClassScannerContext(Map<String, List<Object>> entities) implements IClassScannerContext {

	SimpleClassScannerContext(@NonNull Map<String, List<Object>> entities) {
		this.entities = new HashMap<>(entities);
	}

	SimpleClassScannerContext() {
		this(Collections.emptyMap());
	}

	@NonNull
	@Override
	public Map<String, List<Object>> getEntities() {
		return Collections.unmodifiableMap(this.entities);
	}

	@Override
	public IClassScannerContext withEntity(@NonNull String name, @NonNull Object entity) {
		this.entities.computeIfAbsent(name, (__) -> new ArrayList<>()).add(entity);
		return this;
	}

	@Override
	public IClassScannerContext withoutEntity(@NonNull String name, @NonNull Object entity) {
		List<Object> entities = this.entities.get(name);
		if (entities == null) {
			return this;
		}
		entities.remove(entity);
		if (entities.isEmpty()) {
			this.entities.remove(name);
		}
		return this;
	}

	@Override
	public IClassScannerContext withoutEntity(@NonNull Object entity) {
		this.entities.entrySet().removeIf((entry) -> {
			entry.getValue().remove(entity);
			return entry.getValue().isEmpty();
		});
		return this;
	}

	@Override
	public IClassScannerContext withoutEntities(@NonNull String name) {
		this.entities.remove(name);
		return this;
	}

}
