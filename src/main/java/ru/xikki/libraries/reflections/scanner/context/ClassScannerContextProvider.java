package ru.xikki.libraries.reflections.scanner.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public final class ClassScannerContextProvider {

	@Getter
	@Setter
	@NonNull
	private Function<Map<String, List<Object>>, IClassScannerContext> factory = SimpleClassScannerContext::new;

}
