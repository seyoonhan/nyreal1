package com.han.startup.support;

import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class DefaultModelConverter implements ModelConverterContext {

	private final List<DefaultModelConverter> converters;
	private final Map<String, Model> modelByName;
	private final HashMap<Type, Model> modelByType;
	private final Set<Type> processedTypes;

	public DefaultModelConverter(DefaultModelConverter converter) {
		this(new ArrayList<DefaultModelConverter>());
		converters.add(converter);
	}

	public DefaultModelConverter(List<DefaultModelConverter> converters) {
		this.converters = converters;
		modelByName = new TreeMap<String, Model>();
		modelByType = new HashMap<Type, Model>();
		processedTypes = new HashSet<Type>();
	}

	@Override
	public void defineModel(String name, Model model) {
		defineModel(name, model, null, null);
	}

	@Override
	public void defineModel(String name, Model model, Type type, String prevName) {
		if (log.isDebugEnabled()) {
			log.debug(String.format("defineModel %s %s", name, model));
		}
		modelByName.put(name, model);

		if (!StringUtils.isEmpty(prevName)) {
			modelByName.remove(prevName);
		}

		if (type != null) {
			modelByType.put(type, model);
		}
	}

	@Override
	public Property resolveProperty(Type type, Annotation[] annotations) {
		if (log.isDebugEnabled()) {
			log.debug(String.format("resolveProperty %s", type));
		}
		Iterator<DefaultModelConverter> converters = this.getConverters();
		if (converters.hasNext()) {
			DefaultModelConverter converter = converters.next();
			return converter.resolveProperty(type, annotations);
		}
		return null;
	}

	@Override
	public Model resolve(Type type) {
		if (processedTypes.contains(type)) {
			return modelByType.get(type);
		} else {
			processedTypes.add(type);
		}
		if (log.isDebugEnabled()) {
			log.debug(String.format("resolve %s", type));
		}
		Iterator<DefaultModelConverter> converters = this.getConverters();
		Model resolved = null;
		if (converters.hasNext()) {
			DefaultModelConverter converter = converters.next();
			log.debug("trying extension " + converter);
			resolved = converter.resolve(type);
		}
		if (resolved != null) {
			modelByType.put(type, resolved);

			Model resolvedImpl = resolved;
			if (resolvedImpl instanceof ComposedModel) {
				resolvedImpl = ((ComposedModel) resolved).getChild();
			}
			if (resolvedImpl instanceof ModelImpl) {
				ModelImpl impl = (ModelImpl) resolvedImpl;
				if (impl.getName() != null) {
					modelByName.put(impl.getName(), resolved);
				}
			}
		}

		return resolved;
	}

	public Iterator<DefaultModelConverter> getConverters() {
		return converters.iterator();
	}

	public Map<String, Model> getDefinedModels() {
		return Collections.unmodifiableMap(modelByName);
	}
}