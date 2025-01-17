//package com.simibubi.create.foundation.utility;
//
//
///**
// * This model data instance is passed whenever a model is rendered without
// * available in-world context. IBakedModel#getModelData can react accordingly
// * and avoid looking for model data itself
// **/
//public enum VirtualEmptyModelData implements IModelData {
//
//	INSTANCE;
//
//	@Override
//	public boolean hasProperty(ModelProperty<?> prop) {
//		return false;
//	}
//
//	@Override
//	public <T> T getData(ModelProperty<T> prop) {
//		return null;
//	}
//
//	@Override
//	public <T> T setData(ModelProperty<T> prop, T data) {
//		return null;
//	}
//
//}
