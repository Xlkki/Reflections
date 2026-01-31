package ru.xikki.libraries.reflections.processor;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import ru.xikki.libraries.reflections.bcel.BCELUtils;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public abstract class AbstractClassAnnotationProcessor implements IClassProcessor {

	private final Predicate<String> classNamePredicator;
	private final String classAnnotationSignature;

	protected AbstractClassAnnotationProcessor(Predicate<String> classNamePredicator, @NonNull Class<? extends Annotation> classAnnotation) {
		this.classNamePredicator = classNamePredicator;
		this.classAnnotationSignature = BCELUtils.getAnnotationSignature(classAnnotation);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull String className) {
		return this.classNamePredicator == null || this.classNamePredicator.test(className);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return BCELUtils.hasAnnotation(javaClass, this.classAnnotationSignature);
	}

	@Override
	public boolean shouldLoadClass(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return true;
	}

	@Override
	public boolean shouldCreateInstance(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return false;
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
