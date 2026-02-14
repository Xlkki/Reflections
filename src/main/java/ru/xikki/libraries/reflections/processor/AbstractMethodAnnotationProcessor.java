package ru.xikki.libraries.reflections.processor;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import ru.xikki.libraries.reflections.bcel.BCELUtils;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public abstract class AbstractMethodAnnotationProcessor implements IClassProcessor {

	private final Predicate<String> classNameFilter;
	private final String expectedClassAnnotationSignature;
	private final String methodAnnotationSignature;

	protected AbstractMethodAnnotationProcessor(Predicate<String> classNameFilter, Class<? extends Annotation> expectedClassAnnotation, @NonNull Class<? extends Annotation> methodAnnotation) {
		this.classNameFilter = classNameFilter;
		this.expectedClassAnnotationSignature = expectedClassAnnotation == null ? null : BCELUtils.getAnnotationSignature(expectedClassAnnotation);
		this.methodAnnotationSignature = BCELUtils.getAnnotationSignature(methodAnnotation);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull String className) {
		return this.classNameFilter == null || this.classNameFilter.test(className);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		if (this.expectedClassAnnotationSignature != null) {
			if (BCELUtils.hasAnnotation(javaClass, this.expectedClassAnnotationSignature)) {
				return false;
			}
		}
		return BCELUtils.hasAnnotatedMethod(javaClass, this.methodAnnotationSignature);
	}

	@Override
	public boolean shouldLoadClass(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return true;
	}

	@Override
	public boolean shouldCreateInstance(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return BCELUtils.hasAnnotatedNonStaticMethod(javaClass, this.methodAnnotationSignature);
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
