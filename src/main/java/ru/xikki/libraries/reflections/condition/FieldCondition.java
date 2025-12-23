package ru.xikki.libraries.reflections.condition;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(staticName = "create")
public final class FieldCondition extends ClassMemberCondition<Field> {

	@NonNull
	@Override
	public FieldCondition withName(@NonNull String name) {
		return (FieldCondition) super.withName(name);
	}

	@NonNull
	@Override
	public FieldCondition withNative(boolean isNative) {
		return (FieldCondition) super.withNative(isNative);
	}

	@NonNull
	@Override
	public FieldCondition withStatic(boolean isStatic) {
		return (FieldCondition) super.withStatic(isStatic);
	}

	@NonNull
	@Override
	public FieldCondition withPublic(boolean isPublic) {
		return (FieldCondition) super.withPublic(isPublic);
	}

	@NonNull
	@Override
	public FieldCondition withPrivate(boolean isPrivate) {
		return (FieldCondition) super.withPrivate(isPrivate);
	}

	@NonNull
	@Override
	public FieldCondition withProtected(boolean isProtected) {
		return (FieldCondition) super.withProtected(isProtected);
	}

	@NonNull
	@Override
	public FieldCondition withAbstract(boolean isAbstract) {
		return (FieldCondition) super.withAbstract(isAbstract);
	}

	@NonNull
	@Override
	public FieldCondition withFinal(boolean isFinal) {
		return (FieldCondition) super.withFinal(isFinal);
	}

	@NonNull
	@Override
	public FieldCondition withInterface(boolean isInterface) {
		return (FieldCondition) super.withInterface(isInterface);
	}

	@NonNull
	@Override
	public FieldCondition withSynchronized(boolean isSynchronized) {
		return (FieldCondition) super.withSynchronized(isSynchronized);
	}

	@NonNull
	@Override
	public FieldCondition withStrict(boolean isStrict) {
		return (FieldCondition) super.withStrict(isStrict);
	}

	@NonNull
	@Override
	public FieldCondition withDeclaredClass(@NonNull Class<?> clazz) {
		return (FieldCondition) super.withDeclaredClass(clazz);
	}

	@NonNull
	@Override
	public FieldCondition withAnnotation(@NonNull Class<? extends Annotation> annotation) {
		return (FieldCondition) super.withAnnotation(annotation);
	}

	/**
	 * Add condition by field type
	 *
	 * @param clazz Field type
	 *
	 */
	@NonNull
	public FieldCondition withType(@NonNull Class<?> clazz) {
		this.condition = this.condition.and((field) -> field.getType() == clazz);
		return this;
	}

}
