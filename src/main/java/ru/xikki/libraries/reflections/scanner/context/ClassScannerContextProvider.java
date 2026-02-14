package ru.xikki.libraries.reflections.scanner.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ClassScannerContextProvider {

	@Getter
	@Setter
	@NonNull
	private IClassScannerContext.Factory factory = SimpleClassScannerContext::new;

}
