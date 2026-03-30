package ru.xikki.libraries.reflections.modifier;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import ru.xikki.libraries.reflections.bcel.BCELUtils;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public abstract class AbstractFieldAnnotationModifier implements IClassModifier {

	private final Predicate<String> classNameFilter;
	private final String expectedClassAnnotationSignature;
	private final String fieldAnnotationSignature;

	protected AbstractFieldAnnotationModifier(Predicate<String> classNameFilter, Class<? extends Annotation> expectedClassAnnotation, @NonNull Class<? extends Annotation> fieldAnnotation) {
		this.classNameFilter = classNameFilter;
		this.expectedClassAnnotationSignature = expectedClassAnnotation == null ? null : BCELUtils.getAnnotationSignature(expectedClassAnnotation);
		this.fieldAnnotationSignature = BCELUtils.getAnnotationSignature(fieldAnnotation);
	}

	@Override
	public boolean shouldModify(@NonNull IClassModifier.Holder holder, @NonNull String className) {
		return this.classNameFilter == null || this.classNameFilter.test(className);
	}

	@Override
	public boolean shouldModify(@NonNull IClassModifier.Holder holder, @NonNull JavaClass javaClass) {
		if (this.expectedClassAnnotationSignature != null) {
			if (BCELUtils.hasAnnotation(javaClass, this.expectedClassAnnotationSignature)) {
				return false;
			}
		}
		return BCELUtils.hasAnnotatedField(javaClass, this.fieldAnnotationSignature);
	}

}
