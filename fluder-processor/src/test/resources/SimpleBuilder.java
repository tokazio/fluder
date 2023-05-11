package com.example;

public class SimpleBuilder implements SimpleAField, SimpleCreator{

	private java.lang.String aField;

	private SimpleBuilder(){
		super();
	}

	@Override
	public SimpleCreator setAField(java.lang.String in){
		this.aField = in;
		return this;
	}

	@Override
	public Simple build(){
		final Simple out = new Simple();
		try {
			final java.lang.reflect.Field f = Simple.class.getDeclaredField("aField");
			f.setAccessible(true);
			f.set(out,aField);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		return out;
	}

	public static SimpleAField getInstance(){
		return new SimpleBuilder();
	}
}