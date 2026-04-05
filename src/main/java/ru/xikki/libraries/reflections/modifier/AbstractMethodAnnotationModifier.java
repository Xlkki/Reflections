package ru.xikki.libraries.reflections.modifier;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import ru.xikki.libraries.reflections.bcel.BCELUtils;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public abstract class AbstractMethodAnnotationModifier implements IClassModifier {

	protected final Predicate<String> classNameFilter;
	protected final String expectedClassAnnotationSignature;
	protected final String methodAnnotationSignature;

	protected AbstractMethodAnnotationModifier(Predicate<String> classNameFilter, Class<? extends Annotation> expectedClassAnnotation, @NonNull Class<? extends Annotation> methodAnnotation) {
		this.classNameFilter = classNameFilter;
		this.expectedClassAnnotationSignature = expectedClassAnnotation == null ? null : BCELUtils.getAnnotationSignature(expectedClassAnnotation);
		this.methodAnnotationSignature = BCELUtils.getAnnotationSignature(methodAnnotation);
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
		return BCELUtils.hasAnnotatedMethod(javaClass, this.methodAnnotationSignature);
	}

}
