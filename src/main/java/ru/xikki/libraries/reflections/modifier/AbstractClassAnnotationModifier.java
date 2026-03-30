package ru.xikki.libraries.reflections.modifier;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import ru.xikki.libraries.reflections.bcel.BCELUtils;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public abstract class AbstractClassAnnotationModifier implements IClassModifier {

	private final Predicate<String> classNameFilter;
	private final String classAnnotationSignature;

	protected AbstractClassAnnotationModifier(Predicate<String> classNameFilter, @NonNull Class<? extends Annotation> classAnnotation) {
		this.classNameFilter = classNameFilter;
		this.classAnnotationSignature = BCELUtils.getAnnotationSignature(classAnnotation);
	}

	@Override
	public boolean shouldModify(@NonNull IClassModifier.Holder holder, @NonNull String className) {
		return this.classNameFilter == null || this.classNameFilter.test(className);
	}

	@Override
	public boolean shouldModify(@NonNull IClassModifier.Holder holder, @NonNull JavaClass javaClass) {
		return BCELUtils.hasAnnotation(javaClass, this.classAnnotationSignature);
	}
}
