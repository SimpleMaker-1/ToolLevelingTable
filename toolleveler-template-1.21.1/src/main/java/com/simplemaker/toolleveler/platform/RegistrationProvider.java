package com.simplemaker.toolleveler.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

public class RegistrationProvider<T> {

    private final DeferredRegister<T> register;
    private final String modId;
    private final Collection<RegistryObject<T>> entries = new LinkedHashSet<>();

    private RegistrationProvider(ResourceKey<? extends Registry<T>> key, String modId) {
        this.register = DeferredRegister.create(key, modId);
        this.modId = modId;
    }

    public static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> resourceKey, String modId) {
        return new RegistrationProvider<>(resourceKey, modId);
    }

    public static <T> RegistrationProvider<T> get(Registry<T> registry, String modId) {
        return new RegistrationProvider<>(registry.key(), modId);
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        DeferredHolder<T, I> holder = register.register(name, supplier);

        RegistryObject<I> obj = new RegistryObject<>(holder);
        entries.add((RegistryObject<T>) obj);

        return obj;
    }

    public Collection<RegistryObject<T>> getEntries() {
        return entries;
    }

    public String getModId() {
        return modId;
    }

    public DeferredRegister<T> getDeferredRegister() {
        return register;
    }

    public void registerToBus(net.neoforged.bus.api.IEventBus bus) {
        register.register(bus);
    }
}