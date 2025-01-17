package com.simibubi.create.foundation.config;

public class AllConfigs {

//	static Map<ConfigBase, ModConfig.Type> configs = new HashMap<>();

	public static CClient CLIENT;
	public static CCommon COMMON;
	public static CServer SERVER;

//	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
//		Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
//			T config = factory.get();
//			config.registerAll(builder);
//			return config;
//		});
//
//		T config = specPair.getLeft();
//		config.specification = specPair.getRight();
//		configs.put(config, side);
//		return config;
//	}

	public static void register() {
		CLIENT = new CClient();
		ConfigBase.initGroups(CLIENT.getConfig());
		CLIENT.getConfig().init();
		COMMON = new CCommon();
		ConfigBase.initGroups(COMMON.getConfig());
		COMMON.getConfig().init();

		CCommon.register();

		SERVER = new CServer();
		ConfigBase.initGroups(SERVER.getConfig());
		SERVER.getConfig().init();
		CServer.register();
//		CLIENT = register(CClient::new, ModConfig.Type.CLIENT);
//		COMMON = register(CCommon::new, ModConfig.Type.COMMON);
//		SERVER = register(CServer::new, ModConfig.Type.SERVER);
//
//		for (Entry<ConfigBase, Type> pair : configs.entrySet())
//			ModLoadingContext.get()
//				.registerConfig(pair.getValue(), pair.getKey().specification);
	}

//	public static void onLoad(ModConfig.Loading event) {
//		for (Entry<ConfigBase, Type> pair : configs.entrySet())
//			if (pair.getKey().specification == event.getConfig()
//				.getSpec())
//				pair.getKey()
//					.onLoad();
//	}

//	public static void onReload(ModConfig.Reloading event) {
//		for (Entry<ConfigBase, Type> pair : configs.entrySet())
//			if (pair.getKey().specification == event.getConfig()
//				.getSpec())
//				pair.getKey()
//					.onReload();
//	}
}
