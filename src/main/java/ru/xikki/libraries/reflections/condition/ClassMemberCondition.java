package ru.xikki.libraries.reflections.condition;

import lombok.*;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ClassMemberCondition<M extends Member> {

	private Predicate<M> condition = (__) -> true;

	/**
	 * Add condition by member name
	 *
	 * @param name Member name
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withName(@NonNull String name) {
		this.condition = this.condition.and((member) -> member.getName().equals(name));
		return this;
	}

	/**
	 * Add condition by native modifier
	 *
	 * @param isNative Native modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withNative(boolean isNative) {
		this.condition = this.condition.and((member) -> Modifier.isNative(member.getModifiers()) == isNative);
		return this;
	}

	/**
	 * Add condition by static modifier
	 *
	 * @param isStatic Static modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withStatic(boolean isStatic) {
		this.condition = this.condition.and((member) -> Modifier.isStatic(member.getModifiers()) == isStatic);
		return this;
	}

	/**
	 * Add condition by public modifier
	 *
	 * @param isPublic Public modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withPublic(boolean isPublic) {
		this.condition = this.condition.and((member) -> Modifier.isPublic(member.getModifiers()) == isPublic);
		return this;
	}

	/**
	 * Add condition by private modifier
	 *
	 * @param isPrivate Private modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withPrivate(boolean isPrivate) {
		this.condition = this.condition.and((member) -> Modifier.isPrivate(member.getModifiers()) == isPrivate);
		return this;
	}

	/**
	 * Add condition by protected modifier
	 *
	 * @param isProtected Protected modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withProtected(boolean isProtected) {
		this.condition = this.condition.and((member) -> Modifier.isProtected(member.getModifiers()) == isProtected);
		return this;
	}

	/**
	 * Add condition by abstract modifier
	 *
	 * @param isAbstract Abstract modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withAbstract(boolean isAbstract) {
		this.condition = this.condition.and((member) -> Modifier.isAbstract(member.getModifiers()) == isAbstract);
		return this;
	}

	/**
	 * Add condition by final modifier
	 *
	 * @param isFinal Final modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withFinal(boolean isFinal) {
		this.condition = this.condition.and((member) -> Modifier.isFinal(member.getModifiers()) == isFinal);
		return this;
	}

	/**
	 * Add condition by interface modifier
	 *
	 * @param isInterface Interface modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withInterface(boolean isInterface) {
		this.condition = this.condition.and((member) -> Modifier.isInterface(member.getModifiers()) == isInterface);
		return this;
	}

	/**
	 * Add condition by synchronized modifier
	 *
	 * @param isSynchronized Synchronized modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withSynchronized(boolean isSynchronized) {
		this.condition = this.condition.and((member) -> Modifier.isSynchronized(member.getModifiers()) == isSynchronized);
		return this;
	}

	/**
	 * Add condition by strict modifier
	 *
	 * @param isStrict Strict modifier value
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withStrict(boolean isStrict) {
		this.condition = this.condition.and((member) -> Modifier.isStrict(member.getModifiers()) == isStrict);
		return this;
	}

	/**
	 * Add condition by member declared class
	 *
	 * @param clazz Member declared class
	 *
	 */
	@NonNull
	public ClassMemberCondition<M> withDeclaredClass(@NonNull Class<?> clazz) {
		this.condition = this.condition.and((member) -> member.getDeclaringClass() == clazz);
		return this;
	}

	/**
	 * Return condition as java Predicate
	 *
	 */
	@NonNull
	public Predicate<M> asPredicate() {
		return condition;
	}

}
