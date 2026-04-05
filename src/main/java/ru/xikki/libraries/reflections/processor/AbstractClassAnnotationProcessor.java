package ru.xikki.libraries.reflections.processor;

import lombok.NonNull;
import org.apache.bcel.classfile.JavaClass;
import ru.xikki.libraries.reflections.bcel.BCELUtils;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public abstract class AbstractClassAnnotationProcessor implements IClassProcessor {

	protected final Predicate<String> classNameFilter;
	protected final String classAnnotationSignature;

	protected AbstractClassAnnotationProcessor(Predicate<String> classNameFilter, @NonNull Class<? extends Annotation> classAnnotation) {
		this.classNameFilter = classNameFilter;
		this.classAnnotationSignature = BCELUtils.getAnnotationSignature(classAnnotation);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull String className) {
		return this.classNameFilter == null || this.classNameFilter.test(className);
	}

	@Override
	public boolean shouldProcess(@NonNull IClassProcessor.Holder holder, @NonNull JavaClass javaClass) {
		return BCELUtils.hasAnnotation(javaClass, this.classAnnotationSignature);
	}

}
