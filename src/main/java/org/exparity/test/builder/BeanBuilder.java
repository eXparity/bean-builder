
package org.exparity.test.builder;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.exparity.test.builder.InstanceFactories.InstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.it.modular.beans.BeanNamingStrategy;
import uk.co.it.modular.beans.BeanPropertyPath;
import uk.co.it.modular.beans.Type;
import uk.co.it.modular.beans.TypeProperty;
import uk.co.it.modular.beans.naming.ForceRootNameNamingStrategy;
import uk.co.it.modular.beans.naming.LowerCaseNamingStrategy;
import static java.lang.System.identityHashCode;
import static org.apache.commons.lang.StringUtils.lowerCase;
import static org.exparity.test.builder.InstanceFactories.*;
import static uk.co.it.modular.beans.Type.type;

/**
 * Builder object for instantiating and populating objects which follow the Java beans standards conventions for getter/setters
 * 
 * @author Stewart Bissett
 */
@SuppressWarnings({
		"rawtypes", "unchecked"
})
public class BeanBuilder<T> {

	private static final Logger LOG = LoggerFactory.getLogger(BeanBuilder.class);

	/**
	 * Return an instance of a {@link BeanBuilder} for the given type which can then be populated with values either manually or automatically. For example:
	 * 
	 * <pre>
	 * BeanUtils.anInstanceOf(Person.class).build();
	 * </pre>
	 * @param type the type to return the {@link BeanBuilder} for
	 */
	public static <T> BeanBuilder<T> anInstanceOf(final Class<T> type) {
		return new BeanBuilder<T>(type, BeanBuilderType.NULL, new LowerCaseNamingStrategy());
	}

	/**
	 * Return an instance of a {@link BeanBuilder} for the given type which can then be populated with values either manually or automatically. For example:
	 * 
	 * <pre>
	 * BeanUtils.anInstanceOf(Person.class, &quot;person&quot;).build();
	 * </pre>
	 * @param type the type to return the {@link BeanBuilder} for
	 * @param rootName the name give to the root entity when referencing paths
	 */
	public static <T> BeanBuilder<T> anInstanceOf(final Class<T> type, final String rootName) {
		return new BeanBuilder<T>(type, BeanBuilderType.NULL, new ForceRootNameNamingStrategy(new LowerCaseNamingStrategy(), rootName));
	}

	/**
	 * Return an instance of a {@link BeanBuilder} for the given type which is populated with empty objects but collections, maps, etc which have empty objects. For example:
	 * 
	 * <pre>
	 * BeanUtils.anEmptyInstanceOf(Person.class).build();
	 * </pre>
	 * @param type the type to return the {@link BeanBuilder} for
	 */
	public static <T> BeanBuilder<T> anEmptyInstanceOf(final Class<T> type) {
		return new BeanBuilder<T>(type, BeanBuilderType.EMPTY, new LowerCaseNamingStrategy());
	}

	/**
	 * Return an instance of a {@link BeanBuilder} for the given type which is populated with empty objects but collections, maps, etc which have empty objects. For example:
	 * 
	 * <pre>
	 * BeanUtils.anEmptyInstanceOf(Person.class).build();
	 * </pre>
	 * @param type the type to return the {@link BeanBuilder} for
	 * @param rootName the name give to the root entity when referencing paths
	 */
	public static <T> BeanBuilder<T> anEmptyInstanceOf(final Class<T> type, final String rootName) {
		return new BeanBuilder<T>(type, BeanBuilderType.EMPTY, new ForceRootNameNamingStrategy(new LowerCaseNamingStrategy(), rootName));
	}

	/**
	 * Return an instance of a {@link BeanBuilder} for the given type which is populated with random values. For example:
	 * 
	 * <pre>
	 * BeanUtils.aRandomInstanceOf(Person.class).build();
	 * </pre>
	 * @param type the type to return the {@link BeanBuilder} for
	 */
	public static <T> BeanBuilder<T> aRandomInstanceOf(final Class<T> type) {
		return new BeanBuilder<T>(type, BeanBuilderType.RANDOM, new LowerCaseNamingStrategy());
	}

	/**
	 * Return an instance of a {@link BeanBuilder} for the given type which is populated with random values. For example:
	 * 
	 * <pre>
	 * BeanUtils.aRandomInstanceOf(Person.class).build();
	 * </pre>
	 * @param type the type to return the {@link BeanBuilder} for
	 */
	public static <T> BeanBuilder<T> aRandomInstanceOf(final Class<T> type, final String rootName) {
		return new BeanBuilder<T>(type, BeanBuilderType.RANDOM, new ForceRootNameNamingStrategy(new LowerCaseNamingStrategy(), rootName));
	}

	private final Set<String> excludedProperties = new HashSet<String>();
	private final Set<String> excludedPaths = new HashSet<String>();
	private final Map<String, InstanceFactory> paths = new HashMap<String, InstanceFactory>();
	private final Map<String, InstanceFactory> properties = new HashMap<String, InstanceFactory>();
	private final Map<Class<?>, InstanceFactory> types = new HashMap<Class<?>, InstanceFactory>();
	private final Class<T> type;
	private final BeanBuilderType builderType;
	private final BeanNamingStrategy naming;;
	private CollectionSize defaultCollectionSize = new CollectionSize(1, 5);
	private final Map<String, CollectionSize> collectionSizeForPaths = new HashMap<String, CollectionSize>();
	private final Map<String, CollectionSize> collectionSizeForProperties = new HashMap<String, CollectionSize>();

	private BeanBuilder(final Class<T> type, final BeanBuilderType builderType, final BeanNamingStrategy naming) {
		this.type = type;
		this.builderType = builderType;
		this.naming = naming;
	}

	public BeanBuilder<T> with(final String propertyOrPathName, final Object value) {
		return with(propertyOrPathName, theValue(value));
	}

	public <V> BeanBuilder<T> with(final Class<V> type, final InstanceFactory<V> factory) {
		this.types.put(type, factory);
		return this;
	}

	public BeanBuilder<T> with(final String propertyOrPathName, final InstanceFactory<?> factory) {
		path(propertyOrPathName, factory);
		property(propertyOrPathName, factory);
		return this;
	}

	/**
	 * @deprecated See {@link #property(String, Object)}
	 */
	@Deprecated
	public BeanBuilder<T> withPropertyValue(final String propertyName, final Object value) {
		return property(propertyName, value);
	}

	public BeanBuilder<T> property(final String propertyName, final Object value) {
		return property(propertyName, theValue(value));
	}

	public BeanBuilder<T> property(final String propertyName, final InstanceFactory<?> factory) {
		properties.put(lowerCase(propertyName), factory);
		return this;
	}

	public BeanBuilder<T> excludeProperty(final String propertyName) {
		this.excludedProperties.add(lowerCase(propertyName));
		return this;
	}

	public BeanBuilder<T> path(final String path, final Object value) {
		return path(path, theValue(value));
	}

	public BeanBuilder<T> path(final String path, final InstanceFactory<?> factory) {
		this.paths.put(lowerCase(path), factory);
		return this;
	}

	public BeanBuilder<T> excludePath(final String path) {
		this.excludedPaths.add(lowerCase(path));
		return this;
	}

	public BeanBuilder<T> collectionSizeOf(final int size) {
		return collectionSizeRangeOf(size, size);
	}

	public BeanBuilder<T> collectionSizeRangeOf(final int min, final int max) {
		this.defaultCollectionSize = new CollectionSize(min, max);
		return this;
	}

	public BeanBuilder<T> collectionSizeRangeForPropertyOf(final String property, final int min, final int max) {
		this.collectionSizeForProperties.put(property, new CollectionSize(min, max));
		return this;
	}

	public BeanBuilder<T> collectionSizeForPropertyOf(final String property, final int size) {
		return collectionSizeRangeForPropertyOf(property, size, size);
	}

	public BeanBuilder<T> collectionSizeForPathOf(final String path, final int size) {
		return collectionSizeRangeForPathOf(path, size, size);
	}

	public BeanBuilder<T> collectionSizeRangeForPathOf(final String path, final int min, final int max) {
		this.collectionSizeForPaths.put(path, new CollectionSize(min, max));
		return this;
	}

	public <X> BeanBuilder<T> subtype(final Class<X> klass, final Class<? extends X> subtypes) {
		return with(klass, oneOf(createInstanceOfFactoriesForTypes(subtypes)));
	}

	public <X> BeanBuilder<T> subtype(final Class<X> klass, final Class<? extends X>... subtypes) {
		return with(klass, oneOf(createInstanceOfFactoriesForTypes(subtypes)));
	}

	public T build() {
		return populate(createNewInstance(), new BeanPropertyPath(type(type).camelName()), new Stack(type(type)));
	}

	private <I> I populate(final I instance, final BeanPropertyPath path, final Stack stack) {
		if (instance != null) {
			for (TypeProperty property : type(instance).setNamingStrategy(naming).propertyList()) {
				populateProperty(instance, property, path.append(property.getName()), stack);
			}
			return instance;
		} else {
			return instance;
		}
	}

	private void populateProperty(final Object instance, final TypeProperty property, final BeanPropertyPath path, final Stack stack) {

		if (isExcludedPath(path) || isExcludedProperty(property)) {
			LOG.trace("Ignore [{}]. Explicity excluded", path);
			return;
		}

		InstanceFactory factory = factoryForPath(property, path);
		if (factory != null) {
			assignValue(instance, property, path, createValue(factory, type), stack);
			return;
		}

		if (isPropertySet(instance, property) || isChildOfAssignedPath(path) || isOverflowing(property, path, stack)) {
			return;
		}

		if (property.isArray()) {
			property.setValue(instance, createArray(property.getType().getComponentType(), path, stack));
		} else if (property.isMap()) {
			property.setValue(instance, createMap(property.getTypeParameter(0), property.getTypeParameter(1), collectionSize(path), path, stack));
		} else if (property.isSet()) {
			property.setValue(instance, createSet(property.getTypeParameter(0), collectionSize(path), path, stack));
		} else if (property.isList() || property.isCollection()) {
			property.setValue(instance, createList(property.getTypeParameter(0), collectionSize(path), path, stack));
		} else {
			assignValue(instance, property, path, createValue(property.getType()), stack);
		}
	}

	private boolean isPropertySet(final Object instance, final TypeProperty property) {
		if (property.getValue(instance) == null) {
			return false;
		} else if (property.isCollection()) {
			return !property.getValue(instance, Collection.class).isEmpty();
		} else if (property.isMap()) {
			return !property.getValue(instance, Map.class).isEmpty();
		} else {
			return true;
		}
	}

	private boolean isOverflowing(final TypeProperty property, final BeanPropertyPath path, final Stack stack) {
		if (stack.contains(property.getType())) {
			LOG.trace("Ignore {}. Avoids stack overflow caused by type {}", path, property.getTypeSimpleName());
			return true;
		}
		for (Class<?> genericType : property.getTypeParameters()) {
			if (stack.contains(genericType)) {
				LOG.trace("Ignore {}. Avoids stack overflow caused by type {}", path, genericType.getSimpleName());
				return true;
			}
		}

		return false;
	}

	private InstanceFactory factoryForPath(final TypeProperty property, final BeanPropertyPath path) {
		return selectNotNull(paths.get(path.fullPath()), paths.get(path.fullPathWithNoIndexes()), properties.get(property.getName()));
	}

	private boolean isExcludedProperty(final TypeProperty property) {
		return excludedProperties.contains(property.getName());
	}

	private boolean isExcludedPath(final BeanPropertyPath path) {
		return excludedPaths.contains(path.fullPath()) || excludedPaths.contains(path.fullPathWithNoIndexes());
	}

	private boolean isChildOfAssignedPath(final BeanPropertyPath path) {
		for (String assignedPath : paths.keySet()) {
			if (path.startsWith(assignedPath + ".")) {
				LOG.trace("Ignore {}. Child of assigned path {}", path, assignedPath);
				return true;
			}
		}
		return false;
	}

	private void assignValue(final Object instance, final TypeProperty property, final BeanPropertyPath path, final Object value, final Stack stack) {
		if (value != null) {
			LOG.trace("Assign {} value [{}:{}]", new Object[] {
					path, value.getClass().getSimpleName(), identityHashCode(value)
			});
			property.setValue(instance, populate(value, path, stack.append(type(value))));
		} else {
			LOG.trace("Assign {} value [null]", path);
		}
	}

	private <E> E createValue(final Class<E> type) {

		for (Entry<Class<?>, InstanceFactory> keyedFactory : types.entrySet()) {
			if (type.isAssignableFrom(keyedFactory.getKey())) {
				InstanceFactory factory = keyedFactory.getValue();
				return (E) createValue(factory, type);
			}
		}

		switch (builderType) {
			case RANDOM: {
				InstanceFactory factory = RANDOM_FACTORIES.get(type);
				if (factory != null) {
					return (E) createValue(factory, type);
				} else if (type.isEnum()) {
					return createValue(aRandomEnum(type), type);
				} else {
					return createValue(aNewInstanceOf(type), type);
				}
			}
			case EMPTY: {
				InstanceFactory factory = EMPTY_FACTORIES.get(type);
				if (factory != null) {
					return (E) createValue(factory, type);
				} else if (type.isEnum()) {
					return null;
				} else {
					return createValue(aNewInstanceOf(type), type);
				}
			}
			default:
				return null;
		}
	}

	private <E> E createValue(final InstanceFactory<E> factory, final Class<E> type) {
		E value = factory != null ? factory.createValue() : null;
		LOG.trace("Create Value [{}] for Type [{}]", value, type(type).simpleName());
		return value;
	}

	private <E> Object createArray(final Class<E> type, final BeanPropertyPath path, final Stack stack) {
		switch (builderType) {
			case EMPTY:
			case RANDOM:
				Object array = Array.newInstance(type, collectionSize(path));
				for (int i = 0; i < Array.getLength(array); ++i) {
					Array.set(array, i, populate(createValue(type), path.appendIndex(i), stack.append(type)));
				}
				return array;
			default:
				return null;
		}
	}

	private <E> Set<E> createSet(final Class<E> type, final int length, final BeanPropertyPath path, final Stack stack) {
		switch (builderType) {
			case EMPTY:
			case RANDOM:
				Set<E> set = new HashSet<E>();
				for (int i = 0; i < length; ++i) {
					E value = populate(createValue(type), path.appendIndex(i), stack.append(type));
					if (value != null) {
						set.add(value);
					}
				}
				return set;
			default:
				return null;
		}
	}

	private <E> List<E> createList(final Class<E> type, final int length, final BeanPropertyPath path, final Stack stack) {
		switch (builderType) {
			case EMPTY:
			case RANDOM:
				List<E> list = new ArrayList<E>();
				for (int i = 0; i < length; ++i) {
					E value = populate(createValue(type), path.appendIndex(i), stack.append(type));
					if (value != null) {
						list.add(value);
					}
				}
				return list;
			default:
				return null;
		}
	}

	private <K, V> Map<K, V> createMap(final Class<K> keyType, final Class<V> valueType, final int length, final BeanPropertyPath path, final Stack stack) {
		switch (builderType) {
			case EMPTY:
			case RANDOM:
				Map<K, V> map = new HashMap<K, V>();
				for (int i = 0; i < length; ++i) {
					K key = populate(createValue(keyType), path.appendIndex(i), stack.append(keyType).append(valueType));
					if (key != null) {
						map.put(key, createValue(valueType));
					}
				}
				return map;
			default:
				return null;
		}
	}

	private T createNewInstance() {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			throw new BeanBuilderException("Failed to instantiate '" + type + "'. Error [" + e.getMessage() + "]", e);
		} catch (IllegalAccessException e) {
			throw new BeanBuilderException("Failed to instantiate '" + type + "'. Error [" + e.getMessage() + "]", e);
		}
	}

	private int collectionSize(final BeanPropertyPath path) {
		return selectNotNull(collectionSizeForPaths.get(path.fullPath()),
				collectionSizeForPaths.get(path.fullPathWithNoIndexes()),
				collectionSizeForProperties.get(propertyName(path)),
				defaultCollectionSize).aRandomSize();
	}

	private String propertyName(final BeanPropertyPath path) {
		return StringUtils.substringAfterLast(path.fullPathWithNoIndexes(), ".");
	}

	private <X> List<InstanceFactory<X>> createInstanceOfFactoriesForTypes(final Class<? extends X>... subtypes) {
		List<InstanceFactory<X>> factories = new ArrayList<InstanceFactory<X>>();
		for (Class<? extends X> subtype : subtypes) {
			factories.add((InstanceFactory<X>) aNewInstanceOf(subtype));
		}
		return factories;
	}

	private static class Stack {

		private final Type[] stack;

		private Stack(final Type type) {
			this(new Type[] {
					type
			});
		}

		private Stack(final Type[] stack) {
			this.stack = stack;
		}

		public boolean contains(final Class<?> type) {
			int hits = 0;
			for (Type entry : stack) {
				if (entry.is(type)) {
					if ((++hits) > 1) {
						return true;
					}
				}
			}
			return false;
		}

		public Stack append(final Class<?> value) {
			return append(type(value));
		}

		public Stack append(final Type value) {
			Type[] newStack = Arrays.copyOf(stack, stack.length + 1);
			newStack[stack.length] = value;
			return new Stack(newStack);
		}
	}

	private static class CollectionSize {

		private final int min, max;

		public CollectionSize(final int min, final int max) {
			this.min = min;
			this.max = max;
		}

		public int aRandomSize() {
			if (min == max) {
				return min;
			} else {
				return RandomBuilder.aRandomInteger(min, max);
			}
		}
	}

	@SuppressWarnings("serial")
	private static final Map<Class<?>, InstanceFactory> RANDOM_FACTORIES = new HashMap<Class<?>, InstanceFactory>() {

		{
			put(Short.class, aRandomShort());
			put(short.class, aRandomShort());
			put(Integer.class, aRandomInteger());
			put(int.class, aRandomInteger());
			put(Long.class, aRandomLong());
			put(long.class, aRandomLong());
			put(Double.class, aRandomDouble());
			put(double.class, aRandomDouble());
			put(Float.class, aRandomFloat());
			put(float.class, aRandomFloat());
			put(Boolean.class, aRandomBoolean());
			put(boolean.class, aRandomBoolean());
			put(Byte.class, aRandomByte());
			put(byte.class, aRandomByte());
			put(Character.class, aRandomChar());
			put(char.class, aRandomChar());
			put(String.class, aRandomString());
			put(BigDecimal.class, aRandomDecimal());
			put(Date.class, aRandomDate());
		}
	};

	private static final Map<Class<?>, InstanceFactory> EMPTY_FACTORIES = new HashMap<Class<?>, InstanceFactory>() {

		private static final long serialVersionUID = 1L;

		{
			put(Short.class, aNullValue());
			put(short.class, theValue((short) 0));
			put(Integer.class, aNullValue());
			put(int.class, theValue(0));
			put(Long.class, aNullValue());
			put(long.class, theValue(0));
			put(Double.class, aNullValue());
			put(double.class, theValue(0.0));
			put(Float.class, aNullValue());
			put(float.class, theValue((float) 0.0));
			put(Boolean.class, aNullValue());
			put(boolean.class, theValue(false));
			put(Byte.class, aNullValue());
			put(byte.class, theValue((byte) 0));
			put(Character.class, aNullValue());
			put(char.class, theValue((char) 0));
			put(String.class, aNullValue());
			put(BigDecimal.class, aNullValue());
			put(Date.class, aNullValue());
		}
	};

	private static enum BeanBuilderType {
		RANDOM, EMPTY, NULL
	}

	private <T> T selectNotNull(final T... options) {
		for (T option : options) {
			if (option != null) {
				return option;
			}
		}
		return null;
	}

}