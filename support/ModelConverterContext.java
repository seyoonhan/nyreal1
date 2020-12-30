package com.han.startup.support;

import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

public interface ModelConverterContext {

	public void defineModel(String name, Model model);

	public void defineModel(String name, Model model, Type type, String prevName);

	public Property resolveProperty(Type type, Annotation[] annotations);

	public Model resolve(Type type);

	public Iterator<DefaultModelConverter> getConverters();
}