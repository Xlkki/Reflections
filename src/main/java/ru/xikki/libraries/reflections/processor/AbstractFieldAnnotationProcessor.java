package ru.xikki.libraries.reflections.processor;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import ru.xikki.libraries.reflections.bcel.BCELUtils;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public abstract class AbstractFieldAnnotationProcessor implements IClassProcessor {

	private final Predicate<String> classNamePredicator;
	private final String expectedClassAnnotationSignature;
	private final String fieldAnnotationSignature;

	protected AbstractFieldAnnotationProcessor(Predicate<String> classNamePredicator, Class<? extends Annotation> expectedClassAnnotation, @NonNull Class<? extends Annotation> fieldAnnotation) {
		this.classNamePredicator = classNamePredicator;
		this.expectedClassAnnotationSignature = expectedClassAnnotation == null ? null : BCELUtils.getAnnotationSignature(expectedClassAnnotation);
		this.fieldAnnotationSignature = BCELUtils.getAnnotationSignature(fieldAnnotation);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull String className) {
		return this.classNamePredicator == null || this.classNamePredicator.test(className);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		if (this.expectedClassAnnotationSignature != null) {
			if (BCELUtils.hasAnnotation(javaClass, this.expectedClassAnnotationSignature)) {
				return false;
			}
		}
		return BCELUtils.hasAnnotatedField(javaClass, this.fieldAnnotationSignature);
	}

	@Override
	public boolean shouldLoadClass(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return true;
	}

	@Override
	public boolean shouldCreateInstance(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return BCELUtils.hasAnnotatedNonStaticField(javaClass, this.fieldAnnotationSignature);
	}

	@Override
	public void processInstance(@NonNull IClassProcessor.Holder holder, @NonNull Object instance) {

	}

	@Override
	public void processClass(@NonNull IClassProcessor.Holder holder, @NonNull Class<?> clazz) {

	}

	@Override
	public void processClass(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {

	}

}
